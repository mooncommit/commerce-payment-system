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

    @Transactional
    public Payment createPendingPayment(Order order, PaymentMethodType paymentMethodType) {
        Payment payment = Payment.createPending(order, paymentMethodType);

        return paymentRepository.save(payment);
    }

    // 결제 확정 요청 기본 검증
    @Transactional
    public PaymentConfirmResponse confirmPayment(Long memberId, PaymentConfirmRequest request) {
        Payment payment = getReadyPayment(memberId, request);

        // 이미 처리된 결제는 다시 확정할 수 없도록 차단
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        // 아직 PortOne 검증 전이므로 서버에 저장된 결제 정보만 응답에 담아 반환
        return toConfirmResponse(payment);
    }

    @Transactional
    public PaymentConfirmResponse approvePayment(Long memberId, PaymentConfirmRequest request) {
        Payment payment = getReadyPayment(memberId, request);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        payment.markPaid();
        payment.getOrder().markAsConfirmed();

        return toConfirmResponse(payment);
    }

    @Transactional
    public void failPayment(Long memberId, PaymentConfirmRequest request, String failureReason) {
        Payment payment = getReadyPayment(memberId, request);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        payment.markFailed(failureReason);
        payment.getOrder().markAsCancelled();
    }

    private Payment getReadyPayment(Long memberId, PaymentConfirmRequest request) {
        // 서버에 저장된 결제 ID와 주문 소유자 기준으로 결제 정보 조회
        Payment payment = paymentRepository.findByIdAndOrder_Member_Id(request.getPaymentId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!Objects.equals(payment.getPortonePaymentId(), request.getPortonePaymentId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ID_MISMATCH);
        }

        return payment;
    }

    private PaymentConfirmResponse toConfirmResponse(Payment payment) {
        Order order = payment.getOrder();

        return PaymentConfirmResponse.builder()
                .paymentId(payment.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .portonePaymentId(payment.getPortonePaymentId())
                .paymentStatus(payment.getStatus())
                .orderStatus(order.getOrderStatus())
                .totalAmount(payment.getTotalOrderAmount())
                .usedPointAmount(payment.getUsedPointAmount())
                .pgAmount(payment.getPgAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
