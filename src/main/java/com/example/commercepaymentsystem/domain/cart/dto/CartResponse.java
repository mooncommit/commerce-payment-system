package com.example.commercepaymentsystem.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> items;

    @Getter
    @Builder
    public static class CartItemResponse {
        private Long productId;
        private String productName;
        private Long price;
        private int quantity;
    }
}