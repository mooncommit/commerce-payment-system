package com.example.commercepaymentsystem.domain.order.entity;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private Long usedPointAmount;

    @Column(nullable = false)
    private Long pgAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    public void markAsConfirmed() {
        changeStatus(OrderStatus.COMPLETED);
    }

    public void markAsCancelled() {
        changeStatus(OrderStatus.CANCELED);
    }

    // 주문 상태 변경 로직
    private void changeStatus(OrderStatus newStatus) {
        if (!this.orderStatus.canTransitTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.orderStatus = newStatus;
    }
}
