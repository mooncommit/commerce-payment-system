package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
        return PaymentConfirmResponse.builder()
                .paymentId(request.getPaymentId())
                .portonePaymentId(request.getPortonePaymentId())
                .build();
    }
}