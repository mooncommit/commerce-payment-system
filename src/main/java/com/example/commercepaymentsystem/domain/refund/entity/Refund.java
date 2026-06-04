package com.example.commercepaymentsystem.domain.refund.entity;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 전체 환불만 지원하므로 결제(Payment) 1개당 환불(Refund) 1개만 매핑됩니다 (@OneToOne)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus refundStatus;

    @Column(length = 255)
    private String reason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Builder
    public Refund(Payment payment, String reason) {
        this.payment = payment;
        this.refundStatus = RefundStatus.REQUESTED;
        this.reason = reason;
    }

    // 환불 완료 처리 메서드
    public void markAsCompleted() {
        this.refundStatus = RefundStatus.COMPLETED;
        this.refundedAt = LocalDateTime.now();
    }

    // 환불 실패 처리 메서드
    public void markAsFailed() {
        this.refundStatus = RefundStatus.FAILED;
    }
}