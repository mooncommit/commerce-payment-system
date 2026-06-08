package com.example.commercepaymentsystem.domain.order.dto;

import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreviewOrderItemResponse {

    private Long cartItemId;
    private Long productId;
    private String productName;
    private Long price;
    private Integer quantity;
    private Long lineTotalAmount;
    private Integer stockQuantity;
    private String saleStatus;

    public static PreviewOrderItemResponse from(CartItem cartItem) {
        Product product = cartItem.getProduct();
        Integer quantity = cartItem.getQuantity();
        Long price = product.getPrice();

        return new PreviewOrderItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                price,
                quantity,
                price * quantity,
                product.getStockQuantity(),
                product.getSaleStatus().name()
        );
    }
}
