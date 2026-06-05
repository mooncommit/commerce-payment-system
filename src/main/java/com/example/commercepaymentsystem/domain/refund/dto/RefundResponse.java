package com.example.commercepaymentsystem.domain.refund.dto;

import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RefundResponse {

    private Long refundId;
    private Long paymentId;
    private Long refundPgAmount;
    private Long refundPointAmount;
    private RefundStatus refundStatus;
    private String reason;
    private LocalDateTime refundedAt;

    public static RefundResponse from(Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getId())
                .paymentId(refund.getPayment().getId())
                .refundPgAmount(refund.getRefundPgAmount())
                .refundPointAmount(refund.getRefundPointAmount())
                .refundStatus(refund.getRefundStatus())
                .reason(refund.getReason())
                .refundedAt(refund.getRefundedAt())
                .build();
    }
}
