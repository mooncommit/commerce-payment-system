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
public class OrderCancelResponse {

    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private Long paymentId;
    private PaymentStatus paymentStatus;
    private List<OrderCancelItemResponse> restoredItems;
    private LocalDateTime canceledAt;

    public static OrderCancelResponse from(
            Order order,
            Payment payment,
            List<OrderCancelItemResponse> restoredItems
    ) {
        return new OrderCancelResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus(),
                payment.getId(),
                payment.getStatus(),
                restoredItems,
                order.getCanceledAt()
        );
    }
}
