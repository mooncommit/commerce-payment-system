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
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundFacade {

    private static final String DEFAULT_REFUND_REASON = "사용자 요청 취소";

    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentGateway paymentGateway;

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

        Refund refund = paymentCommandService.refundPaymentAndOrder(paymentId, reason);

        try {
            paymentGateway.cancelPayment(payment.getPortonePaymentId(), reason);
        } catch (Exception e) {
            log.error("PG 환불 실패: DB 환불 요청은 생성됨, 수동 처리 필요. portonePaymentId={}",
                    payment.getPortonePaymentId(), e);
        }

        return RefundResponse.from(refund);
    }

    private String resolveReason(RefundRequest request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            return DEFAULT_REFUND_REASON;
        }
        return request.getReason();
    }
}
