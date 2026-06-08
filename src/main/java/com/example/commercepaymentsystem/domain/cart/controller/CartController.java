package com.example.commercepaymentsystem.domain.cart.controller;

import com.example.commercepaymentsystem.domain.cart.dto.CartResponse;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니에 상품 담기
    @PostMapping("/items")
    public ApiResponse<Void> addItem(
            @RequestParam Long productId,
            @RequestParam int quantity) {

        // 1. SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 객체에서 memberId를 추출합니다.
        Long memberId = (Long) authentication.getPrincipal();
        cartService.addItem(memberId, productId, quantity);

        return ApiResponse.success("장바구니에 상품이 담겼습니다.");
    }

    // 장바구니 조회
    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        // 1. SecurityContext에서 인증된 memberId 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = (Long) authentication.getPrincipal();

        CartResponse cartResponse = cartService.getCart(memberId);

        return ApiResponse.success(cartResponse, "장바구니 조회 성공");
    }


}