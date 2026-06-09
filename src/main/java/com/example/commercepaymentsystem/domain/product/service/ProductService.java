package com.example.commercepaymentsystem.domain.product.service;

import com.example.commercepaymentsystem.domain.product.dto.ProductRequest;
import com.example.commercepaymentsystem.domain.product.dto.ProductResponse;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import com.example.commercepaymentsystem.global.dto.PageResponse; // 글로벌 DTO 사용
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 조회: 락을 걸고 상품 조회
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

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

    // 전체 조회
    public PageResponse<ProductResponse> findAllProducts(String categoryCode, String statusString, Pageable pageable) {
        SaleStatus saleStatus = null;
        if (statusString != null && !statusString.isBlank()) {
            try {
                saleStatus = SaleStatus.valueOf(statusString);
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        String category = (categoryCode != null && !categoryCode.isBlank()) ? categoryCode : null;

        Page<Product> productPage = productRepository.findAllByFilters(category, saleStatus, pageable);

        List<ProductResponse> content = productPage.getContent().stream()
                .map(ProductResponse::from)
                .toList();

        return new PageResponse<>(
                content,
                productPage.getNumber()+1,
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
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

        // 삭제 로직 변경: 물리적 삭제 대신 Soft Delete 적용
        product.markAsDeleted();
    }

}
