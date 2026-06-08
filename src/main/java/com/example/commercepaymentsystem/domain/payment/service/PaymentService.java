package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentMethodType;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제 대기 상태의 결제를 생성한다.
     *
     * @param order 결제가 연결될 주문
     * @param paymentMethodType 결제 수단
     * @return 저장된 결제 엔티티
     */
    @Transactional
    public Payment createPendingPayment(Order order, PaymentMethodType paymentMethodType) {
        Payment payment = Payment.createPending(order, paymentMethodType);
        return paymentRepository.save(payment);
    }

    /**
     * 결제 확정 요청에 대한 기본 검증을 수행하고 응답을 반환한다.
     *
     * <p>이 단계에서는 PG 검증 전이므로 결제 상태를 바꾸지 않고,
     * 서버에 저장된 결제 정보만 응답으로 내려준다.
     *
     * @param memberId 요청한 회원 ID
     * @param request 결제 확정 요청 정보
     * @return 결제 확정 응답
     */
    public PaymentConfirmResponse confirmPayment(Long memberId, PaymentConfirmRequest request) {
        Payment payment = findReadyPayment(memberId, request);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        return toConfirmResponse(payment);
    }

    /**
     * 결제 확정에 필요한 결제 엔티티를 조회하고 요청값을 검증한다.
     *
     * @param memberId 요청한 회원 ID
     * @param request 결제 요청 정보
     * @return 검증을 통과한 결제 엔티티
     */
    @Transactional
    public Payment findReadyPayment(Long memberId, PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByIdAndOrder_Member_Id(request.getPaymentId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!Objects.equals(payment.getPortonePaymentId(), request.getPortonePaymentId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ID_MISMATCH);
        }

        return payment;
    }

    @Transactional
    public Payment findByIdWithOrder(Long paymentId) {
        return paymentRepository.findByIdWithOrder(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    // 웹훅에서 수신한 PortOne 쪽 paymentId, 즉 portonePaymentId 기반으로 Payment 조회
    @Transactional
    public Payment findByPortonePaymentId(String portonePaymentId) {
        return paymentRepository.findByPortonePaymentId(portonePaymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 결제 상태를 완료로 변경한다.
     *
     * @param payment 상태를 바꿀 결제
     */
    @Transactional
    public void markPaid(Payment payment) {
        markPaid(payment, null);
    }

    /**
     * 결제 상태를 완료로 변경하고 PG 거래 ID를 저장한다.
     *
     * @param payment 상태를 바꿀 결제
     * @param portoneTransactionId PG 거래 ID
     */
    @Transactional
    public void markPaid(Payment payment, String portoneTransactionId) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }
        payment.markPaid(portoneTransactionId);
    }

    /**
     * 결제 상태를 실패로 변경한다.
     *
     * @param payment 상태를 바꿀 결제
     * @param failureReason 실패 사유
     */
    @Transactional
    public void markFailed(Payment payment, String failureReason) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }
        payment.markFailed(failureReason);
    }

    /**
     * 결제 상태를 취소로 변경한다.
     *
     * @param payment 상태를 바꿀 결제
     */
    public void cancelPayment(Payment payment) {
        payment.markCanceled();
    }

    /**
     * 결제 확정 응답 DTO를 생성한다.
     *
     * @param payment 응답으로 변환할 결제
     * @return 결제 확정 응답
     */
    public PaymentConfirmResponse toConfirmResponse(Payment payment) {
        Order order = payment.getOrder();

        return PaymentConfirmResponse.builder()
                .paymentId(payment.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .paymentStatus(payment.getStatus())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .usedPointAmount(order.getUsedPointAmount())
                .pgAmount(order.getPgAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
