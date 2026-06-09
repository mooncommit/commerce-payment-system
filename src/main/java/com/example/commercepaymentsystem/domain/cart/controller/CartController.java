package com.example.commercepaymentsystem.domain.cart.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.cart.dto.CartItemResponse;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ApiResponse<Void> addItem(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestBody com.example.commercepaymentsystem.domain.cart.dto.CartAddRequest request) {
        cartService.addItem(loginMember.getMemberId(), request.getProductId(), request.getQuantity());
        return ApiResponse.success("장바구니에 상품이 담겼습니다.");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CartItemResponse>>> getCartItems(
            @AuthenticationPrincipal LoginMember loginMember,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CartItemResponse> response = cartService.getCartItems(loginMember.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "장바구니 목록 조회 성공"));
    }

    // 수량 변경
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long cartItemId,
            @RequestBody com.example.commercepaymentsystem.domain.cart.dto.CartUpdateRequest request) {
        cartService.updateQuantity(loginMember.getMemberId(), cartItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("수량이 변경되었습니다."));
    }

    // 상품 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long cartItemId) {
        cartService.removeItem(loginMember.getMemberId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("장바구니에서 상품이 삭제되었습니다."));
    }

    // 전체 비우기
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal LoginMember loginMember) {
        cartService.clearCart(loginMember.getMemberId());
        return ResponseEntity.ok(ApiResponse.success("장바구니를 비웠습니다."));
    }
}