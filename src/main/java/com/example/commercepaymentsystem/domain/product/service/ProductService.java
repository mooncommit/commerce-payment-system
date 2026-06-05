package com.example.commercepaymentsystem.domain.product.service;

import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.commercepaymentsystem.domain.product.dto.ProductRequest;
import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Product findProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional
    public void restoreStock(Product product, Integer quantity) {
        product.restoreStock(quantity);
    }

    @Transactional
    public void createProduct(ProductRequest request) {
        Product product = new Product(
                request.getCategoryCode(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                SaleStatus.ON_SALE // 기본값 설정
        );
        productRepository.save(product);
    }
}
