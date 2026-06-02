package com.example.commercepaymentsystem.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {

    private Long paymentId;

    private String portonePaymentId;
}