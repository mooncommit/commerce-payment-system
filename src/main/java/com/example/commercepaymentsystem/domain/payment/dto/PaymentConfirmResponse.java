package com.example.commercepaymentsystem.domain.payment.dto;

import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentConfirmResponse {

    private Long paymentId;

    private Long orderId;

    private String orderNumber;

    private String portonePaymentId;

    private PaymentStatus paymentStatus;

    private OrderStatus orderStatus;

    private Long totalAmount;

    private Long usedPointAmount;

    private Long pgAmount;

    private LocalDateTime paidAt;
}