package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrderListResponse {

    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private Long totalAmount;
    private Long usedPointAmount;
    private Long pgAmount;
    private Long earnedPointAmount;
    private LocalDateTime createdAt;
}
