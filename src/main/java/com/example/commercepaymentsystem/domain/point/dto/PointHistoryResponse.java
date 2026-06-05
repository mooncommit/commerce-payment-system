package com.example.commercepaymentsystem.domain.point.dto;

import com.example.commercepaymentsystem.domain.point.enums.PointType;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PointHistoryResponse {

    private final Long id;
    private final Long paymentId;
    private final PointType type;
    private final Long amount;
    private final String reason;
    private final LocalDateTime createdAt;

    public PointHistoryResponse(Long id, Long paymentId, PointType type, Long amount, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = createdAt;
    }
}
