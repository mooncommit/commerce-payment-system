package com.example.commercepaymentsystem.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PreviewOrderResponse {

    private List<PreviewOrderItemResponse> items;
    private Long totalAmount;
    private Long memberPointBalance;

    public static PreviewOrderResponse from(List<PreviewOrderItemResponse> items, Long memberPointBalance) {
        Long totalAmount = items.stream()
                .mapToLong(PreviewOrderItemResponse::getLineTotalAmount)
                .sum();

        return new PreviewOrderResponse(items, totalAmount, memberPointBalance);
    }
}
