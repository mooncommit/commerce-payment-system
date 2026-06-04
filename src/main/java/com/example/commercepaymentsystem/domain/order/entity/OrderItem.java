package com.example.commercepaymentsystem.domain.order.entity;

import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private Long unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long lineTotalAmount;

    public static OrderItem create(Order order, Product product, Integer quantity) {
        // 상품 정보가 나중에 바뀌어도 주문 당시 상품명과 가격 유지
        OrderItem orderItem = new OrderItem();
        orderItem.order = order;
        orderItem.product = product;
        orderItem.productName = product.getName();
        orderItem.unitPrice = product.getPrice();
        orderItem.quantity = quantity;
        orderItem.lineTotalAmount = product.getPrice() * quantity;
        return orderItem;
    }
}
