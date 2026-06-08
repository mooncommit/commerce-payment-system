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
    public ResponseEntity<Page<CartItemResponse>> getCartItems(
            @PageableDefault(size = 10) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(cartService.getCartItems(memberId, pageable));
    }
}