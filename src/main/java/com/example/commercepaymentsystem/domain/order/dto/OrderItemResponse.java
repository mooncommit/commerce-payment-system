package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemResponse {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private Long unitPrice;
    private Integer quantity;
    private Long lineTotalAmount;

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProductName(),
                orderItem.getUnitPrice(),
                orderItem.getQuantity(),
                orderItem.getLineTotalAmount()
        );
    }
}
