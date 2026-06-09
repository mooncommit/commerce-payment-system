package com.example.commercepaymentsystem.domain.cart.dto;

import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private int quantity;
    private Long unitPrice;
    private int stock;

    public static CartItemResponse from(CartItem cartItem) {
        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getProduct().getPrice())
                .stock(cartItem.getProduct().getStockQuantity())
                .build();
    }
}