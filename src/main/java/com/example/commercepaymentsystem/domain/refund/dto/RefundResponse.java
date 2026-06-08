package com.example.commercepaymentsystem.domain.refund.dto;

import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RefundResponse {

    private Long refundId;
    private Long paymentId;
    private Long orderId;
    private Long refundPgAmount;
    private Long refundPointAmount;
    private RefundStatus refundStatus;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime refundedAt;

    public static RefundResponse from(Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getId())
                .paymentId(refund.getPayment().getId())
                .orderId(refund.getPayment().getOrder().getId())
                .refundPgAmount(refund.getRefundPgAmount())
                .refundPointAmount(refund.getRefundPointAmount())
                .refundStatus(refund.getRefundStatus())
                .orderStatus(refund.getPayment().getOrder().getOrderStatus())
                .paymentStatus(refund.getPayment().getStatus())
                .reason(refund.getReason())
                .createdAt(refund.getCreatedAt())
                .refundedAt(refund.getRefundedAt())
                .build();
    }
}
