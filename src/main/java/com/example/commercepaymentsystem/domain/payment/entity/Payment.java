package com.example.commercepaymentsystem.domain.payment.entity;

import com.example.commercepaymentsystem.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    private Long id;

    private Long orderId;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethodType;

    private Long totalOrderAmount;

    private Long usedPointAmount;

    private Long pgAmount;

    private String portonePaymentId;

    private String portoneTransactionId;


}
