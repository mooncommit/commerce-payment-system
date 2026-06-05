package com.example.commercepaymentsystem.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebhookPayload {
    private String imp_uid;
    private String merchant_uid;
    private String status;
}
