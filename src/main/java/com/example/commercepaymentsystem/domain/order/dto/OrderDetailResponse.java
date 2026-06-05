package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
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

    public static OrderDetailResponse from(Order order, Payment payment, List<OrderDetailItemResponse> items) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus(),
                payment.getId(),
                payment.getStatus(),
                order.getTotalAmount(),
                order.getUsedPointAmount(),
                order.getPgAmount(),
                order.getEarnedPointAmount(),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getCanceledAt(),
                items
        );
    }
}
