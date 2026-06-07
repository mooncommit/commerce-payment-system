package com.example.commercepaymentsystem.domain.order.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.order.dto.CartOrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts/orders")
public class CartOrderController {

    private final OrderService orderService;

    // 장바구니 상품을 기준으로 주문과 결제 대기 데이터 함께 생성
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createCartOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CartOrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createCartOrder(loginMember.getMemberId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "장바구니 주문 및 결제 대기 생성 성공"));
    }
}
