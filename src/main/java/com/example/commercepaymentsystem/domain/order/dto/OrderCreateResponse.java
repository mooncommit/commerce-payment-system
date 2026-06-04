package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderCreateResponse {

    private Long orderId;
    private String orderNumber;
    private Long paymentId;
    private String portonePaymentId;
    private OrderStatus orderStatus;
    private String paymentStatus;
    private Long totalAmount;
    private Long usedPointAmount;
    private Long pgAmount;
    private List<OrderItemResponse> items;
}
