package com.example.commercepaymentsystem.domain.payment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentApprovedEvent {
    private final Long memberId;
    private final Long orderId;
    private final Long pgAmount;
    private final Long usedPointAmount;
}
