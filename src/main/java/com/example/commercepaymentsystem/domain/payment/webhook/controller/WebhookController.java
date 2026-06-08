package com.example.commercepaymentsystem.domain.payment.webhook.controller;

import com.example.commercepaymentsystem.global.response.ApiResponse;
import com.example.commercepaymentsystem.domain.payment.webhook.PortOneWebhookVerifier;
import com.example.commercepaymentsystem.domain.payment.webhook.WebhookHandler;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PortOneWebhookVerifier portOneWebhookVerifier;
    private final WebhookHandler webhookHandler;

    @PostMapping("/portone")
    public ResponseEntity<ApiResponse<Void>> handlePortOneWebhook(
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestBody String body) {

        log.info("[Webhook] received id={} timestamp={}", webhookId, webhookTimestamp);

        // 1. 시그니처 검증 : 실패 시 200 + 경고 로그 (standard-webhooks 권고)
        Webhook webhook;
        try {
            webhook = portOneWebhookVerifier.verify(body, webhookId, webhookSignature, webhookTimestamp);
        } catch (WebhookVerificationException e) {
            log.warn("[Webhook] verification failed id={} reason={}", webhookId, e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("웹훅 수신 성공"));
        }

        // 2. 검증 통과 : 핸들러로 위임
        webhookHandler.handle(webhookId, webhook, body);
        return ResponseEntity.ok(ApiResponse.success("웹훅 수신 성공"));
    }

}

