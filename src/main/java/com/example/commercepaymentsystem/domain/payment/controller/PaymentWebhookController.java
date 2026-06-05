package com.example.commercepaymentsystem.domain.payment.controller;

import com.example.commercepaymentsystem.domain.payment.dto.WebhookPayload;
import com.example.commercepaymentsystem.domain.payment.facade.PaymentWebhookFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentWebhookFacade paymentWebhookFacade;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody WebhookPayload payload) {
        // 포트원 웹훅은 정상 수신 시 200 OK만 응답하면 됨
        paymentWebhookFacade.processWebhook(payload);
        return ResponseEntity.ok("success");
    }
}
