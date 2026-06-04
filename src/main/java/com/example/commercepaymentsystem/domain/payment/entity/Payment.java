package com.example.commercepaymentsystem.domain.payment.entity;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentMethodType;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethodType;

    private Long totalOrderAmount;

    private Long usedPointAmount;

    private Long pgAmount;

    private Long earnedPointAmount;

    @Column(name = "portone_payment_id", nullable = false, unique = true, length = 200)
    private String portonePaymentId;

    private String portoneTransactionId;

    private LocalDateTime paidAt;

    private LocalDateTime failedAt;

    private String failureReason;

    private static String generatePortonePaymentId() {
        return "pay_" + UUID.randomUUID();
    }

    public static Payment createPending(Order order, PaymentMethodType paymentMethodType) {
        Payment payment = new Payment();
        payment.order = order;
        payment.memberId = order.getMember().getId();
        payment.status = PaymentStatus.PENDING;
        payment.paymentMethodType = paymentMethodType;
        payment.totalOrderAmount = order.getTotalAmount();
        payment.usedPointAmount = order.getUsedPointAmount();
        payment.pgAmount = order.getPgAmount();
        payment.earnedPointAmount = 0L;
        payment.portonePaymentId = generatePortonePaymentId();
        return payment;
    }

    public void markPaid(String portoneTransactionId, Long earnedPointAmount) {
        changeStatus(PaymentStatus.COMPLETED);
        this.portoneTransactionId = portoneTransactionId;
        this.earnedPointAmount = earnedPointAmount;
        this.paidAt = LocalDateTime.now();
    }

    public void markFailed(String failureReason) {
        changeStatus(PaymentStatus.FAILED);
        this.failureReason = failureReason;
        this.failedAt = LocalDateTime.now();
    }

    public void markCanceled() {
        changeStatus(PaymentStatus.CANCELED);
    }

    public void markRefunded() {
        changeStatus(PaymentStatus.REFUNDED);
    }

    // 결제 상태 변경 로직
    private void changeStatus(PaymentStatus newStatus) {
        if (!this.status.canTransitTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        this.status = newStatus;
    }
}
