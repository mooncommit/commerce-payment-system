package com.example.commercepaymentsystem.domain.payment.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.facade.PaymentFacade;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        PaymentConfirmResponse response = paymentFacade.confirmPayment(loginMember, request);

        return ResponseEntity.ok(ApiResponse.success(response, "결제 확정 성공"));
    }
}
