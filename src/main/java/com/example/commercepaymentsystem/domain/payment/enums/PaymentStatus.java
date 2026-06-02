package com.example.commercepaymentsystem.domain.payment.enums;

public enum PaymentStatus {
    READY,      // 결제 생성
    PAID,       // 결제 완료
    FAILED,     // 결제 실패
    CANCELED    // 환불 완료
}