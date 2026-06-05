package com.example.commercepaymentsystem.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderDetailItemResponse {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private Long unitPrice;
    private Integer quantity;
    private Long lineTotalAmount;
}
