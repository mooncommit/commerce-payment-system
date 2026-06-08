package com.example.commercepaymentsystem.domain.cart.entity;


import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    private boolean deleted = false;

    @Builder
    public CartItem(Cart cart, Product product, int quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    // 수량 증가
    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    // 수량 수정
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    // 수량 삭제
    public void delete() {
        this.deleted = true;
    }

    // 삭제 확인
    public boolean isDeleted() {
        return this.deleted;
    }

    public void restore() {
        this.deleted = false;
    }
}