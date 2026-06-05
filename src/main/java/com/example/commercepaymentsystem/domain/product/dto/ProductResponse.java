package com.example.commercepaymentsystem.domain.product.dto;

import com.example.commercepaymentsystem.domain.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private Long price;
    private Integer stockQuantity;

    // Entity를 DTO로 변환하는 메서드
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}