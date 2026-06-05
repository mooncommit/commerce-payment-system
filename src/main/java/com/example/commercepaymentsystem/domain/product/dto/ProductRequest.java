package com.example.commercepaymentsystem.domain.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductRequest {
    private String categoryCode;
    private String name;
    private String description;
    private Long price;
    private Integer stockQuantity;
}