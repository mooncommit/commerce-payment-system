package com.example.commercepaymentsystem.domain.refund.service;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import com.example.commercepaymentsystem.domain.refund.repository.RefundRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefundServiceTest {

    @Test
    void createRefundCreatesRequestedRefundWithoutCompletedAt() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        RefundService refundService = new RefundService(refundRepository);
        Payment payment = newPaidPayment(1L, 10L);

        when(refundRepository.findByPayment_Id(1L)).thenReturn(Optional.empty());
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Refund refund = refundService.createRefund(payment, "단순 변심");

        assertEquals(payment, refund.getPayment());
        assertEquals("단순 변심", refund.getReason());
        assertEquals(40_000L, refund.getRefundPgAmount());
        assertEquals(10_000L, refund.getRefundPointAmount());
        assertEquals(RefundStatus.REQUESTED, refund.getRefundStatus());
        assertNull(refund.getRefundedAt());
        verify(refundRepository).save(refund);
    }

    @Test
    void createRefundRetriesFailedRefundAsRequested() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        RefundService refundService = new RefundService(refundRepository);
        Payment payment = newPaidPayment(1L, 10L);
        Refund refund = Refund.builder()
                .payment(payment)
                .refundPgAmount(40_000L)
                .refundPointAmount(10_000L)
                .reason("기존 사유")
                .build();
        refund.markAsFailed();

        when(refundRepository.findByPayment_Id(1L)).thenReturn(Optional.of(refund));

        Refund result = refundService.createRefund(payment, "재시도 사유");

        assertEquals(refund, result);
        assertEquals(RefundStatus.REQUESTED, refund.getRefundStatus());
        assertEquals("재시도 사유", refund.getReason());
        assertNull(refund.getRefundedAt());
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void createRefundRejectsCompletedRefund() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        RefundService refundService = new RefundService(refundRepository);
        Payment payment = newPaidPayment(1L, 10L);
        Refund refund = Refund.builder()
                .payment(payment)
                .refundPgAmount(40_000L)
                .refundPointAmount(10_000L)
                .reason("단순 변심")
                .build();
        refund.markAsCompleted();

        when(refundRepository.findByPayment_Id(1L)).thenReturn(Optional.of(refund));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundService.createRefund(payment, "재시도 사유")
        );

        assertEquals(ErrorCode.INVALID_REFUND_STATUS, exception.getErrorCode());
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void markCompletedChangesRefundStatusToCompleted() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        RefundService refundService = new RefundService(refundRepository);
        Refund refund = Refund.builder()
                .payment(newPaidPayment(1L, 10L))
                .refundPgAmount(40_000L)
                .refundPointAmount(10_000L)
                .reason("단순 변심")
                .build();

        when(refundRepository.findById(2L)).thenReturn(Optional.of(refund));

        Refund result = refundService.markCompleted(2L);

        assertEquals(refund, result);
        assertEquals(RefundStatus.COMPLETED, refund.getRefundStatus());
    }

    @Test
    void markFailedChangesRefundStatusToFailed() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        RefundService refundService = new RefundService(refundRepository);
        Refund refund = Refund.builder()
                .payment(newPaidPayment(1L, 10L))
                .refundPgAmount(40_000L)
                .refundPointAmount(10_000L)
                .reason("단순 변심")
                .build();

        when(refundRepository.findById(2L)).thenReturn(Optional.of(refund));

        refundService.markFailed(2L);

        assertEquals(RefundStatus.FAILED, refund.getRefundStatus());
    }

    private static Payment newPaidPayment(Long paymentId, Long memberId) {
        Member member = newEntity(Member.class);
        setField(member, "id", memberId);

        Order order = newEntity(Order.class);
        setField(order, "member", member);
        setField(order, "pgAmount", 40_000L);
        setField(order, "usedPointAmount", 10_000L);

        Payment payment = newEntity(Payment.class);
        setField(payment, "id", paymentId);
        setField(payment, "order", order);
        setField(payment, "status", PaymentStatus.COMPLETED);
        return payment;
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
