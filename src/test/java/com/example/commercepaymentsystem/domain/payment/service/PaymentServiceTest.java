package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
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
