package com.example.commercepaymentsystem.domain.refund.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.refund.dto.RefundRequest;
import com.example.commercepaymentsystem.domain.refund.dto.RefundResponse;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.service.RefundService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/{paymentId}/refunds")
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<ApiResponse<RefundResponse>> requestRefund(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundRequest request
    ) {
        Refund refund = refundService.requestRefund(paymentId, loginMember.getMemberId(), request.getReason());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(RefundResponse.from(refund), "환불 요청 생성 성공"));
    }
}
