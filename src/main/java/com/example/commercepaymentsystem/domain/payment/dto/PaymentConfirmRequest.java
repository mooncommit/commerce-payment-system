package com.example.commercepaymentsystem.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {

    @NotNull(message = "결제 ID는 필수입니다")
    private Long paymentId;

    @NotBlank(message = "PortOne 결제 ID는 필수입니다")
    private String portonePaymentId;
}