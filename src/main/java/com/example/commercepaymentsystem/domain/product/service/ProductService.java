package com.example.commercepaymentsystem.domain.product.service;

import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.commercepaymentsystem.domain.product.dto.ProductRequest;
import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;

import java.util.List;
import java.util.stream.Collectors;

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

    // 상품 등록
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

    // 상품 전체 조회
    public List<ProductResponse> findAllProducts() {
        // 상품 전체 조회 후 DTO 리스트로 변환
        return productRepository.findAll().stream()
                .map(ProductResponse::from) //메서드 참조 방식
                .toList();
    }

}
