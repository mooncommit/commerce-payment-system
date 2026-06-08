package com.example.commercepaymentsystem.domain.payment.facade;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGateway;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;
import com.example.commercepaymentsystem.domain.payment.service.PaymentCommandService;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 결제 승인 유스케이스 Facade
 * 결제 확정은 생성된 Payment를 기준으로 처리한다.
 * 현재 단계에서는 결제 확정 요청 검증과 응답 생성을 PaymentService에 위임한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {

    // PortOne 결제 완료 상태값. 문자열 비교 시 매직 스트링을 피하기 위해 상수화.
    private static final String PG_STATUS_PAID = "PAID";

    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentGateway paymentGateway;

    public PaymentConfirmResponse confirmPayment(LoginMember loginMember, PaymentConfirmRequest request) {
        Long memberId = loginMember.getMemberId();
        PaymentConfirmResponse readyPayment = paymentService.confirmPayment(memberId, request);
        String portonePaymentId = request.getPortonePaymentId();

        // PG 결제 금액이 0원인 경우(전액 포인트 결제), PortOne 조회를 생략하고 바로 승인 처리
        if (readyPayment.getPgAmount() == 0) {
            log.info("PG 결제 금액 0원 - PortOne 조회 생략: paymentId={}", readyPayment.getPaymentId());
            return paymentCommandService.approvePaymentAndOrder(memberId, request);
        }

        PaymentGatewayResponse pgPayment = paymentGateway.getPayment(portonePaymentId);

        if (!PG_STATUS_PAID.equals(pgPayment.status())) {
            log.warn("결제 확정 실패 - PG 결제 미완료: paymentId={}, pgStatus={}",
                    readyPayment.getPaymentId(), pgPayment.status());
            paymentCommandService.failPaymentAndOrder(memberId, request, "PG 결제가 완료되지 않았습니다.");
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }

        if (!Objects.equals(readyPayment.getPgAmount(), pgPayment.totalAmount())) {
            log.error("결제 확정 실패 - 금액 불일치: paymentId={}, dbPgAmount={}, pgAmount={}",
                    readyPayment.getPaymentId(), readyPayment.getPgAmount(), pgPayment.totalAmount());
            paymentCommandService.failPaymentAndOrder(memberId, request, "결제 금액이 일치하지 않습니다.");
            try {
                paymentGateway.cancelPayment(portonePaymentId, "결제 금액 불일치 자동 취소");
            } catch (Exception e) {
                log.error("PG 자동 취소 실패: portonePaymentId={}", portonePaymentId, e);
                throw new BusinessException(ErrorCode.PG_CANCEL_FAILED);
            }
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        try {
            return paymentCommandService.approvePaymentAndOrder(memberId, request);
        } catch (Exception e) {
            log.error("DB 결제 상태 확정 중 에러 발생, PG 결제 자동 취소 진행: {}", portonePaymentId, e);
            try {
                paymentGateway.cancelPayment(portonePaymentId, "시스템 에러로 인한 결제 자동 취소");
            } catch (Exception cancelEx) {
                log.error("PG 결제 자동 취소 실패 - 수동 처리 필요: {}", portonePaymentId, cancelEx);
            }
            throw e;
        }
    }
}
