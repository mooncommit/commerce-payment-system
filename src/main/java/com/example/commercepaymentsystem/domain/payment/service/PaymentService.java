package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 결제 확정 요청 기본 검증
    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
        // 서버에 저장된 주문 ID와 PortOne 결제 식별자 기준으로 결제 정보 조회
        Payment payment = paymentRepository.findByOrder_IdAndPortonePaymentId(
                        request.getOrderId(),
                        request.getPortonePaymentId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();

        // 이미 처리된 결제는 다시 확정할 수 없도록 차단
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        // 아직 PortOne 검증 전이므로 서버에 저장된 결제 정보만 응답에 담아 반환
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
                .paidAt(order.getPaidAt())
                .build();
    }
}
