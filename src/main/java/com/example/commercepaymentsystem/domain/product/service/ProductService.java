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
    public void deductStock(Long productId, Integer quantity) {
        // 조회: 상품이 없으면 findProductEntity 내부에서 PRODUCT_NOT_FOUND 예외 발생
        Product product = findProductEntity(productId);

        // 실행: 엔티티의 규칙(reduceStock)을 호출
        product.reduceStock(quantity);
    }

    // 상품 등록
    @Transactional
    public void createProduct(ProductRequest request) {
        // 빌더 패턴 적용
        Product product = Product.builder()
                .categoryCode(request.getCategoryCode())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .saleStatus(SaleStatus.ON_SALE) // 기본값 설정
                .build();

        productRepository.save(product);
    }

    // 상품 전체 조회
    public List<ProductResponse> findAllProducts() {
        // 상품 전체 조회 후 DTO 리스트로 변환
        return productRepository.findAll().stream()
                .map(ProductResponse::from) //메서드 참조 방식
                .toList();
    }

    // 상품 단 건 조회
    public ProductResponse findProduct(Long productId) {
        // 상품을 조회하고, DTO로 변환하여 반환
        Product product = findProductEntity(productId);
        return ProductResponse.from(product);
    }

    // 상품 수정
    @Transactional
    public void updateProduct(Long productId, ProductRequest request) {
        Product product = findProductEntity(productId);

        product.updateProduct(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity()
        );
    }

    // 상품 삭제
    @Transactional
    public void deleteProduct(Long productId) {

        Product product = findProductEntity(productId);
        productRepository.delete(product);
    }

}
