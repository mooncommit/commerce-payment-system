package com.example.commercepaymentsystem.domain.order.enums;

/**
 * 주문 상태 머신
 * - PAYMENT_PENDING → COMPLETED: 결제 성공 → 주문 확정
 * - PAYMENT_PENDING → CANCELED: 결제 실패 또는 취소
 * - COMPLETED → CANCELED: 결제 취소·환불
 */
public enum OrderStatus {
    PAYMENT_PENDING {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return target == COMPLETED || target == CANCELED;
        }
    },
    COMPLETED {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return target == CANCELED;
        }
    },
    CANCELED {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return false;
        }
    };
    public abstract boolean canTransitTo(OrderStatus target);
}

