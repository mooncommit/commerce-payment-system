package com.example.commercepaymentsystem.domain.payment.enums;

/**
 * 결제 상태 머신
 * - READY → PAID     : 결제 성공
 * - READY → FAILED   : 결제 미완료 (PG 실패, 금액 불일치 등)
 * - PAID  → CANCELED : 성공한 결제의 사후 취소 (환불)
 *
 * - FAILED  = 결제가 성공적으로 완료되지 못한 모든 경우
 * - CANCELED = 성공한 결제를 사후에 취소한 경우
 */
public enum PaymentStatus {
    // 결제 생성
    READY {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == PAID || target == FAILED;
        }
    },
    // 결제 완료
    PAID {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == CANCELED;
        }
    },
    // 결제 실패
    FAILED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    },
    // 환불 완료
    CANCELED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(PaymentStatus target);
}
