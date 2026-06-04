package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    @Test
    void approvePaymentMarksPaymentPaidAndOrderCompleted() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);
        Order order = newEntity(Order.class);
        setField(order, "id", 1L);
        setField(order, "orderNumber", "ORD-TEST-001");
        setField(order, "orderStatus", OrderStatus.PAYMENT_PENDING);
        setField(order, "totalAmount", 50_000L);
        setField(order, "usedPointAmount", 10_000L);
        setField(order, "pgAmount", 40_000L);
        Payment payment = newEntity(Payment.class);
        setField(payment, "id", 1L);
        setField(payment, "memberId", 10L);
        setField(payment, "order", order);
        setField(payment, "status", PaymentStatus.PENDING);
        setField(payment, "portonePaymentId", "pay_test");
        setField(payment, "totalOrderAmount", 50_000L);
        setField(payment, "usedPointAmount", 10_000L);
        setField(payment, "pgAmount", 40_000L);
        PaymentConfirmRequest request = newEntity(PaymentConfirmRequest.class);
        setField(request, "paymentId", 1L);
        setField(request, "portonePaymentId", "pay_test");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        paymentService.approvePayment(10L, request, "pg_tx_1", 0L);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getOrderStatus());
    }

    @Test
    void failPaymentMarksPaymentFailedAndOrderCanceled() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);
        Order order = newEntity(Order.class);
        setField(order, "id", 1L);
        setField(order, "orderStatus", OrderStatus.PAYMENT_PENDING);
        Payment payment = newEntity(Payment.class);
        setField(payment, "id", 1L);
        setField(payment, "memberId", 10L);
        setField(payment, "order", order);
        setField(payment, "status", PaymentStatus.PENDING);
        setField(payment, "portonePaymentId", "pay_test");
        PaymentConfirmRequest request = newEntity(PaymentConfirmRequest.class);
        setField(request, "paymentId", 1L);
        setField(request, "portonePaymentId", "pay_test");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        paymentService.failPayment(10L, request, "PG 결제가 완료되지 않았습니다.");

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
    }

    @Test
    void confirmPaymentRejectsPaymentOwnedByAnotherMember() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);
        Payment payment = newEntity(Payment.class);
        setField(payment, "id", 1L);
        setField(payment, "memberId", 10L);
        PaymentConfirmRequest request = newEntity(PaymentConfirmRequest.class);
        setField(request, "paymentId", 1L);
        setField(request, "portonePaymentId", "pay_test");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.confirmPayment(20L, request)
        );

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
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
