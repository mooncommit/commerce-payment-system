package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderDetailResponse {

    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private Long paymentId;
    private PaymentStatus paymentStatus;
    private Long totalAmount;
    private Long usedPointAmount;
    private Long pgAmount;
    private Long earnedPointAmount;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime canceledAt;
    private List<OrderDetailItemResponse> items;
}
