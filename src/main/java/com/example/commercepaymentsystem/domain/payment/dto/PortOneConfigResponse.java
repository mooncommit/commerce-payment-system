package com.example.commercepaymentsystem.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PortOneConfigResponse {
    private String storeId;
    private String channelKey;
}
