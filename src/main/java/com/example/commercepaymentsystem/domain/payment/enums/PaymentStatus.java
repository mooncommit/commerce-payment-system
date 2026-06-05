package com.example.commercepaymentsystem.domain.payment.enums;

/**
 * 결제 상태 머신
 * - PENDING → COMPLETED : 결제 성공
 * - PENDING → FAILED    : 결제 실패 또는 검증 실패 (PG 실패, 금액 불일치 등)
 * - COMPLETED → CANCELED: 결제 취소
 * - COMPLETED → REFUNDED: 환불 완료
 * - PENDING → FAILED   = 결제가 성공적으로 완료되지 못한 경우
 * - COMPLETED  → CANCELED  = 성공한 결제의 사후 취소
 * - REFUNDED = 성공한 결제를 환불 완료한 경우
 */
public enum PaymentStatus {
    // 결제 생성
    PENDING {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == COMPLETED || target == FAILED;
        }
    },
    // 결제 완료
    COMPLETED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == CANCELED || target == REFUNDED;
        }
    },
    // 결제 실패
    FAILED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    },
    // 결제 취소
    CANCELED{
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    },
    // 환불 완료
    REFUNDED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(PaymentStatus target);
}
