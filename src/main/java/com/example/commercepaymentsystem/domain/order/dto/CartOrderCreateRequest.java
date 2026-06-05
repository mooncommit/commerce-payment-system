package com.example.commercepaymentsystem.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CartOrderCreateRequest {

    private List<Long> cartItemIds;
    private Long usePointAmount;
}
