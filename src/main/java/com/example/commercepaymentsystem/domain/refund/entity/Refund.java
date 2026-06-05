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

    // 1:1 결제 엔티티 매핑
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

    @Column(length = 255)
    private String reason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // 서비스 로직에서 환불 객체를 쉽게 만들기 위한 생성자
    @Builder
    public Refund(Payment payment, Long refundPgAmount, Long refundPointAmount, String reason) {
        this.payment = payment;
        this.refundPgAmount = refundPgAmount;
        this.refundPointAmount = refundPointAmount;
        this.reason = reason;
        this.refundStatus = RefundStatus.REQUESTED; // 초기 상태 설정 (필요에 따라 변경)
    }

    // 환불 완료 처리 메서드
    public void markAsCompleted() {
        this.refundStatus = RefundStatus.COMPLETED;
        this.refundedAt = LocalDateTime.now();
    }
}