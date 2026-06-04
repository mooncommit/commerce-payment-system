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

    // PortOne 결제 조회/취소/환불 요청에 사용하는 외부 결제 식별자
    @Column(name = "portone_payment_id", nullable = false, unique = true, length = 200)
    private String portonePaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethodType paymentMethodType;

    // 주문 금액 스냅샷. 주문 금액이 바뀌어도 결제 당시 검증 기준은 유지한다.
    @Column(nullable = false)
    private Long totalOrderAmount;

    @Column(nullable = false)
    private Long usedPointAmount;

    @Column(nullable = false)
    private Long pgAmount;

    private LocalDateTime paidAt;

    private LocalDateTime failedAt;

    private String failureReason;

    private static String generatePortonePaymentId() {
        return "pay_" + UUID.randomUUID();
    }

    public static Payment createPending(Order order, PaymentMethodType paymentMethodType) {
        Payment payment = new Payment();
        payment.order = order;
        payment.status = PaymentStatus.PENDING;
        payment.paymentMethodType = paymentMethodType;
        payment.totalOrderAmount = order.getTotalAmount();
        payment.usedPointAmount = order.getUsedPointAmount();
        payment.pgAmount = order.getPgAmount();
        payment.portonePaymentId = generatePortonePaymentId();
        return payment;
    }

    public void markPaid() {
        changeStatus(PaymentStatus.COMPLETED);
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
