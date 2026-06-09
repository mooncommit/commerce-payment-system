package com.example.commercepaymentsystem.domain.product.dto;

import com.example.commercepaymentsystem.domain.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {
    private Long id;
    private String categoryCode;
    private String name;
    private String description;
    private Long price;
    private Integer stockQuantity;
    private String saleStatus;

    // Entity를 DTO로 변환하는 메서드
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .categoryCode(product.getCategoryCode())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .saleStatus(product.getSaleStatus() != null ? product.getSaleStatus().name() : null)
                .build();
    }
}