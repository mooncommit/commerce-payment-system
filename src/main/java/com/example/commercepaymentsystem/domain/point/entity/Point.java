package com.example.commercepaymentsystem.domain.point.entity;

import com.example.commercepaymentsystem.domain.point.enums.PointType;
import com.example.commercepaymentsystem.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "point_ledger",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_point_ledger_idempotency_key", columnNames = "idempotency_key")
        }
)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType pointType;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balanceAfter;

    private String reason;

    // 같은 결제/환불 후처리가 재호출되어도 포인트 잔액이 두 번 바뀌지 않도록 하는 업무 키입니다.
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    public Point(
            Long memberId,
            Long paymentId,
            PointType pointType,
            Long amount,
            Long balanceAfter,
            String reason,
            String idempotencyKey
    ) {
        this.memberId = memberId;
        this.paymentId = paymentId;
        this.pointType = pointType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.idempotencyKey = idempotencyKey;
    }

    // 결제 성공 후처리용 멱등키입니다. 예: PAYMENT:1:USE, PAYMENT:1:EARN
    public static String paymentKey(Long paymentId, PointType pointType) {
        return "PAYMENT:" + paymentId + ":" + pointType.name();
    }

    // 환불 후처리용 멱등키입니다. 예: REFUND:1:REFUND, REFUND:1:REVOKE
    public static String refundKey(Long refundId, PointType pointType) {
        return "REFUND:" + refundId + ":" + pointType.name();
    }
}
