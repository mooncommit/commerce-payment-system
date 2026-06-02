package com.example.commercepaymentsystem.domain.order.dto.response;

import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PreviewOrderResponse {

    private List<Item> items;
    private Long totalAmount;
    private Long memberPointBalance;

    @Getter
    @AllArgsConstructor
    public static class Item {

        private Long cartItemId;
        private Long productId;
        private String productName;
        private Long price;
        private Integer quantity;
        private Long lineTotalAmount;
        private Integer stockQuantity;
        private SaleStatus saleStatus;
    }
}
