package com.example.commercepaymentsystem.domain.payment.entity;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentMethodType;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTest {

    @Test
    void createPendingCreatesReadyPaymentSnapshotForOrder() {
        Member member = newEntity(Member.class);
        setField(member, "id", 1L);

        Order order = newEntity(Order.class);
        setField(order, "member", member);
        setField(order, "totalAmount", 50_000L);
        setField(order, "usedPointAmount", 10_000L);
        setField(order, "pgAmount", 40_000L);

        Payment payment = Payment.createPending(order, PaymentMethodType.CARD);

        assertEquals(order, payment.getOrder());
        assertEquals(1L, payment.getMemberId());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(PaymentMethodType.CARD, payment.getPaymentMethodType());
        assertEquals(50_000L, payment.getTotalOrderAmount());
        assertEquals(10_000L, payment.getUsedPointAmount());
        assertEquals(40_000L, payment.getPgAmount());
        assertEquals(0L, payment.getEarnedPointAmount());
        assertNotNull(payment.getPortonePaymentId());
        assertTrue(payment.getPortonePaymentId().startsWith("pay_"));
    }

    @Test
    void markCanceledChangesPaidPaymentToCanceled() {
        Payment payment = newEntity(Payment.class);
        setField(payment, "status", PaymentStatus.COMPLETED);

        payment.markCanceled();

        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    }

    private static <T> T newEntity(Class<T> entityType) {
        try {
            Constructor<T> constructor = entityType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
