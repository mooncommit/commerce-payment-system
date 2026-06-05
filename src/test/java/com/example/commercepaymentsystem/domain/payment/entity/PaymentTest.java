package com.example.commercepaymentsystem.domain.payment.entity;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentMethodType;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTest {

    @Test
    void createPendingCreatesPendingPaymentForOrderWithoutDuplicatingOrderValues() {
        Member member = newEntity(Member.class);
        setField(member, "id", 1L);

        Order order = newEntity(Order.class);
        setField(order, "member", member);
        setField(order, "totalAmount", 50_000L);
        setField(order, "usedPointAmount", 10_000L);
        setField(order, "pgAmount", 40_000L);

        Payment payment = Payment.createPending(order, PaymentMethodType.CARD);

        assertEquals(order, payment.getOrder());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(PaymentMethodType.CARD, payment.getPaymentMethodType());
        assertEquals(50_000L, payment.getOrder().getTotalAmount());
        assertEquals(10_000L, payment.getOrder().getUsedPointAmount());
        assertEquals(40_000L, payment.getOrder().getPgAmount());
        assertNotNull(payment.getPortonePaymentId());
        assertTrue(payment.getPortonePaymentId().startsWith("pay_"));

        assertFalse(hasDeclaredField(Payment.class, "memberId"));
        assertFalse(hasDeclaredField(Payment.class, "totalOrderAmount"));
        assertFalse(hasDeclaredField(Payment.class, "usedPointAmount"));
        assertFalse(hasDeclaredField(Payment.class, "pgAmount"));
    }

    @Test
    void paymentMethodIsRequiredColumn() throws NoSuchFieldException {
        Column paymentMethodColumn = Payment.class.getDeclaredField("paymentMethodType")
                .getAnnotation(Column.class);

        assertNotNull(paymentMethodColumn);
        assertFalse(paymentMethodColumn.nullable());
    }

    @Test
    void orderAmountsAreReadFromAssociatedOrder() {
        Member member = newEntity(Member.class);
        setField(member, "id", 1L);

        Order order = newEntity(Order.class);
        setField(order, "member", member);
        setField(order, "totalAmount", 50_000L);
        setField(order, "usedPointAmount", 10_000L);
        setField(order, "pgAmount", 40_000L);

        Payment payment = Payment.createPending(order, PaymentMethodType.CARD);

        setField(order, "totalAmount", 60_000L);
        setField(order, "usedPointAmount", 15_000L);
        setField(order, "pgAmount", 45_000L);

        assertEquals(60_000L, payment.getOrder().getTotalAmount());
        assertEquals(15_000L, payment.getOrder().getUsedPointAmount());
        assertEquals(45_000L, payment.getOrder().getPgAmount());
    }

    @Test
    void markPaidChangesPendingPaymentToCompleted() {
        Payment payment = newEntity(Payment.class);
        setField(payment, "status", PaymentStatus.PENDING);

        payment.markPaid();

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(payment.getPaidAt());
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

    private static boolean hasDeclaredField(Class<?> targetType, String fieldName) {
        return Arrays.stream(targetType.getDeclaredFields())
                .anyMatch(field -> field.getName().equals(fieldName));
    }
}
