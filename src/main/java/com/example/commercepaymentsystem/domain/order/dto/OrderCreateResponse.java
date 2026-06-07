package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
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

    public static OrderCreateResponse from(Order order, Payment payment, List<OrderItemResponse> items) {
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderNumber(),
                payment.getId(),
                payment.getPortonePaymentId(),
                order.getOrderStatus(),
                payment.getStatus().name(),
                order.getTotalAmount(),
                order.getUsedPointAmount(),
                order.getPgAmount(),
                items
        );
    }
}
