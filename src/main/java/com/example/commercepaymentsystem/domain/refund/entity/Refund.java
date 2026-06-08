package com.example.commercepaymentsystem.domain.refund.entity;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    // 전체 환불만 지원하므로 결제(Payment) 1개당 환불(Refund) 1개만 매핑됩니다.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(nullable = false)
    private Long refundPgAmount;

    @Column(nullable = false)
    private Long refundPointAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus refundStatus;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Builder
    public Refund(Payment payment, Long refundPgAmount, Long refundPointAmount, String reason) {
        this.payment = payment;
        this.refundPgAmount = refundPgAmount;
        this.refundPointAmount = refundPointAmount;
        this.reason = reason;
        this.refundStatus = RefundStatus.REQUESTED;
    }

    public void markAsCompleted() {
        this.refundStatus = RefundStatus.COMPLETED;
        this.refundedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.refundStatus = RefundStatus.FAILED;
    }

    public void markAsRequested(String reason) {
        this.reason = reason;
        this.refundStatus = RefundStatus.REQUESTED;
        this.refundedAt = null;
    }
}
