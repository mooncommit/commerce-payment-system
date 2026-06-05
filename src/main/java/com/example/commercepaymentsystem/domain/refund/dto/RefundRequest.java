package com.example.commercepaymentsystem.domain.refund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefundRequest {

    @NotBlank(message = "환불 사유는 필수입니다")
    private String reason;

    public RefundRequest(String reason) {
        this.reason = reason;
    }
}
