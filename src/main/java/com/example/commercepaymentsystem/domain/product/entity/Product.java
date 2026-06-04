package com.example.commercepaymentsystem.domain.product.entity;

import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
}
