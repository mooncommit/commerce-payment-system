package com.example.commercepaymentsystem.domain.product.entity;

import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("sale_status != 'DELETED'")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false, length = 30)
    private SaleStatus saleStatus;

    /**
     * 상품 생성을 위한 빌더 생성자
     * @param saleStatus 판매 상태 (입력되지 않을 경우 기본값 ON_SALE 적용)
     */
    @Builder
    public Product(String categoryCode, String name, String description, Long price, Integer stockQuantity, SaleStatus saleStatus) {
        this.categoryCode = categoryCode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.saleStatus = saleStatus != null ? saleStatus : SaleStatus.ON_SALE;
    }

    public boolean isOnSale() {
        return saleStatus == SaleStatus.ON_SALE;
    }

    public boolean hasStock(Integer quantity) {
        return stockQuantity >= quantity;
    }

    // 주문/재고 도메인에서 주문 생성 시 재고를 먼저 줄일 때 사용
    public void decreaseStock(Integer quantity) {
        this.stockQuantity -= quantity;
    }

    public void markAsDeleted() {
        this.saleStatus = SaleStatus.DELETED; // 만약 DELETED 상태가 없다면 END_OF_SALE 사용
    }

    // 재고 복구 메서드
    public void restoreStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }
        this.stockQuantity += quantity;
    }

    // 재고 차감 로직
    public void reduceStock(Integer quantity) {
        // 현재 재고가 요청 수량보다 적으면 예외 발생
        if (this.stockQuantity < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK); // 재고 부족 에러
        }

        // 재고 차감
        this.stockQuantity -= quantity;
    }

    // 상품 수정용 생성자
    public void updateProduct(String name, String description, Long price, Integer stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
}
