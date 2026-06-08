package com.example.commercepaymentsystem.domain.cart.controller;

import com.example.commercepaymentsystem.domain.cart.dto.CartItemResponse;
import com.example.commercepaymentsystem.domain.cart.service.CartService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ApiResponse<Void> addItem(@RequestParam Long productId, @RequestParam int quantity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = (Long) auth.getPrincipal();
        cartService.addItem(memberId, productId, quantity);
        return ApiResponse.success("장바구니에 상품이 담겼습니다.");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CartItemResponse>>> getCartItems(
            @AuthenticationPrincipal Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CartItemResponse> response = cartService.getCartItems(memberId, pageable);

        // 두 번째 인자로 메시지를 넣어주면 해결됩니다!
        return ResponseEntity.ok(ApiResponse.success(response, "장바구니 목록 조회 성공"));
    }

    // 수량 변경
    @PatchMapping("/items/{productId}")
    public ResponseEntity<Void> updateQuantity(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        cartService.updateQuantity(memberId, productId, quantity);
        return ResponseEntity.ok().build();
    }

    // 상품 삭제
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId) {
        cartService.removeItem(memberId, productId);
        return ResponseEntity.ok().build();
    }


}