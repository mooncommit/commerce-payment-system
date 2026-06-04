package com.example.commercepaymentsystem.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    private Long productId;
    private Integer quantity;
    private Long usePointAmount;
}
