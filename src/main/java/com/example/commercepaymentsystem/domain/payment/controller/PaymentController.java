package com.example.commercepaymentsystem.domain.payment.controller;

import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
            @RequestBody PaymentConfirmRequest request
    ) {
        PaymentConfirmResponse response = paymentService.confirmPayment(request);

        return ResponseEntity.ok(ApiResponse.success(response, "결제 확정 성공"));
    }
}