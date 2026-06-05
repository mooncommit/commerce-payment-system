package com.example.commercepaymentsystem.domain.refund.facade;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGateway;
import com.example.commercepaymentsystem.domain.payment.service.PaymentCommandService;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.domain.refund.dto.RefundRequest;
import com.example.commercepaymentsystem.domain.refund.dto.RefundResponse;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.service.RefundService;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 전액 환불 요청을 처리하는 최상위 퍼사드(Facade) 컴포넌트.
 * 환불 이력 생성, 외부 API(포트원 PG 취소) 통신, 내부 DB 후처리의 순서를 제어한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefundFacade {

    private static final String DEFAULT_REFUND_REASON = "사용자 요청 취소";

    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final RefundService refundService;
    private final PaymentGateway paymentGateway;

    /**
     * 클라이언트의 전액 환불 요청을 처리한다.
     *
     * <p>흐름:
     * <ol>
     *     <li>환불 대상 결제를 조회하고 회원/상태를 검증한다.</li>
     *     <li>Refund를 REQUESTED 상태로 먼저 저장해 환불 시도 이력을 남긴다.</li>
     *     <li>포트원 PG 취소를 호출한다.</li>
     *     <li>PG 취소가 성공하면 결제/주문/재고/포인트를 정리하고 Refund를 COMPLETED로 변경한다.</li>
     *     <li>PG 취소가 실패하면 Refund를 FAILED로 변경하고 결제/주문 상태는 그대로 둔다.</li>
     * </ol>
     *
     * <p>이렇게 남긴 REQUESTED/FAILED 상태는 PG 통신 실패나 후처리 실패 건을 운영자가 추적하는 단서가 된다.
     */
    public RefundResponse requestRefund(LoginMember loginMember, Long paymentId, RefundRequest request) {
        Long memberId = loginMember.getMemberId();
        String reason = resolveReason(request);

        Payment payment = paymentService.findByIdWithOrder(paymentId);

        if (!payment.getOrder().getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_STATUS);
        }

        Refund requestedRefund = refundService.createRefund(payment, reason);

        try {
            paymentGateway.cancelPayment(payment.getPortonePaymentId(), reason);
        } catch (Exception e) {
            refundService.markFailed(requestedRefund.getId());
            log.error("PG 환불 실패: Refund FAILED 처리 완료. refundId={}, portonePaymentId={}",
                    requestedRefund.getId(), payment.getPortonePaymentId(), e);
            throw new BusinessException(ErrorCode.PG_CANCEL_FAILED);
        }

        Refund completedRefund = paymentCommandService.refundPaymentAndOrder(paymentId, requestedRefund.getId());

        return RefundResponse.from(completedRefund);
    }

    /**
     * 환불 사유가 비어있는 경우 기본 사유를 채운다.
     */
    private String resolveReason(RefundRequest request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            return DEFAULT_REFUND_REASON;
        }
        return request.getReason();
    }
}
