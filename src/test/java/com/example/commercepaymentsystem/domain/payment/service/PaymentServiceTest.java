package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    @Test
    void confirmPaymentReturnsReadyResponseForPendingPayment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);
        Order order = newEntity(Order.class);
        setField(order, "id", 1L);
        setField(order, "orderNumber", "ORD-TEST-001");
        setField(order, "orderStatus", OrderStatus.PAYMENT_PENDING);
        setField(order, "totalAmount", 50_000L);
        setField(order, "usedPointAmount", 10_000L);
        setField(order, "pgAmount", 40_000L);
        setField(order, "paidAt", LocalDateTime.of(2026, 1, 1, 10, 0));

        Payment payment = newEntity(Payment.class);
        setField(payment, "id", 1L);
        setField(payment, "order", order);
        setField(payment, "status", PaymentStatus.PENDING);
        setField(payment, "portonePaymentId", "pay_test");
        setField(payment, "paidAt", LocalDateTime.of(2026, 1, 1, 10, 5));

        PaymentConfirmRequest request = newEntity(PaymentConfirmRequest.class);
        setField(request, "paymentId", 1L);
        setField(request, "portonePaymentId", "pay_test");

        when(paymentRepository.findByIdAndOrder_Member_Id(1L, 10L)).thenReturn(Optional.of(payment));

        PaymentConfirmResponse response = paymentService.confirmPayment(10L, request);

        assertEquals(1L, response.getPaymentId());
        assertEquals(1L, response.getOrderId());
        assertEquals("ORD-TEST-001", response.getOrderNumber());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        assertEquals(OrderStatus.PAYMENT_PENDING, response.getOrderStatus());
        assertEquals(50_000L, response.getTotalAmount());
        assertEquals(10_000L, response.getUsedPointAmount());
        assertEquals(40_000L, response.getPgAmount());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 5), response.getPaidAt());
    }

    @Test
    void markPaidChangesPendingPaymentToCompleted() {
        PaymentService paymentService = new PaymentService(mock(PaymentRepository.class));
        Payment payment = newEntity(Payment.class);
        setField(payment, "status", PaymentStatus.PENDING);

        paymentService.markPaid(payment);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
    }

    @Test
    void markFailedChangesPendingPaymentToFailed() {
        PaymentService paymentService = new PaymentService(mock(PaymentRepository.class));
        Payment payment = newEntity(Payment.class);
        setField(payment, "status", PaymentStatus.PENDING);

        paymentService.markFailed(payment, "PG 결제가 완료되지 않았습니다.");

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void confirmPaymentRejectsPaymentOwnedByAnotherMember() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);
        PaymentConfirmRequest request = newEntity(PaymentConfirmRequest.class);
        setField(request, "paymentId", 1L);
        setField(request, "portonePaymentId", "pay_test");

        when(paymentRepository.findByIdAndOrder_Member_Id(1L, 20L)).thenReturn(Optional.empty());

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
