package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCancelItemResponse {

    private Long productId;
    private String productName;
    private Integer restoredQuantity;

    public static OrderCancelItemResponse from(OrderItem orderItem) {
        return new OrderCancelItemResponse(
                orderItem.getProduct().getId(),
                orderItem.getProductName(),
                orderItem.getQuantity()
        );
    }
}
