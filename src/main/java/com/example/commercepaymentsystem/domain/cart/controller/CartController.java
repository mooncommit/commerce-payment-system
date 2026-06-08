package com.example.commercepaymentsystem.domain.cart.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.cart.dto.CartItemResponse;
import com.example.commercepaymentsystem.domain.cart.dto.CartItemQuantityUpdateRequest;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ApiResponse<Void> addItem(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestParam Long productId,
            @RequestParam int quantity
    ) {
        cartService.addItem(loginMember.getMemberId(), productId, quantity);
        return ApiResponse.success("장바구니에 상품이 담겼습니다.");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CartItemResponse>>> getCartItems(
            @AuthenticationPrincipal LoginMember loginMember,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CartItemResponse> response = cartService.getCartItems(loginMember.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "장바구니 목록 조회 성공"));
    }

    @PatchMapping("/items/{productId}")
    public ApiResponse<Void> updateQuantity(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long productId,
            @RequestBody CartItemQuantityUpdateRequest request
    ) {
        cartService.updateQuantity(loginMember.getMemberId(), productId, request.getQuantity());
        return ApiResponse.success("수량이 변경되었습니다.");
    }

    @DeleteMapping("/items/{productId}")
    public ApiResponse<Void> removeItem(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long productId
    ) {
        cartService.removeItem(loginMember.getMemberId(), productId);
        return ApiResponse.success("장바구니에서 상품이 삭제되었습니다.");
    }
}
