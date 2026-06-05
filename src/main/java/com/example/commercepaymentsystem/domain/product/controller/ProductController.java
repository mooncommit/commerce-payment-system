package com.example.commercepaymentsystem.domain.product.controller;

import com.example.commercepaymentsystem.domain.product.dto.ProductRequest;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Void> createProduct(@RequestBody ProductRequest requestDto) {
        productService.createProduct(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts() {
        return ResponseEntity.ok(productService.findAllProducts());
    }
}