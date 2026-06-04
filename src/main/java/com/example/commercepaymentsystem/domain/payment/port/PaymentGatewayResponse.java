package com.example.commercepaymentsystem.domain.payment.port;

public record PaymentGatewayResponse(
        String id,
        String status,
        long totalAmount
) {}
