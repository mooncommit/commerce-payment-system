package com.example.commercepaymentsystem.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PortOneConfigResponse {
    private String storeId;
    private String channelKey;
}
