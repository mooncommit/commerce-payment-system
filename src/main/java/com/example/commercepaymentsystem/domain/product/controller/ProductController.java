package com.example.commercepaymentsystem.domain.product.controller;

import com.example.commercepaymentsystem.domain.product.dto.ProductRequest;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.service.ProductService;
import com.example.commercepaymentsystem.global.response.ApiResponse; // 공통 응답 포맷
import com.example.commercepaymentsystem.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createProduct(@Valid @RequestBody ProductRequest requestDto) {
        productService.createProduct(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("상품 추가 성공"));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<ProductResponse> response = productService.findAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "상품 조회 성공"));
    }

    // 단 건 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long productId) {
        ProductResponse response = productService.findProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response, "상품 조회 성공"));
    }

    // 상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest requestDto) {
        productService.updateProduct(productId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("상품 수정 성공"));
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품 삭제 성공"));
    }
}