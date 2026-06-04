package com.example.commercepaymentsystem.domain.payment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentFailedEvent {
    private final Long orderId;
}
