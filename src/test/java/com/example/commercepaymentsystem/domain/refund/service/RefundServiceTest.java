package com.example.commercepaymentsystem.domain.refund.service;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefundServiceTest {

    @Test
    void createRefundCreatesRequestedRefundWithoutCompletedAt() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        RefundService refundService = new RefundService(refundRepository, mock(PaymentRepository.class));
        Payment payment = newPaidPayment(1L, 10L);

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
    void requestRefundFindsPaidMemberPaymentAndCreatesRefund() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        RefundService refundService = new RefundService(refundRepository, paymentRepository);
        Payment payment = newPaidPayment(1L, 10L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Refund refund = refundService.requestRefund(1L, 10L, "단순 변심");

        assertEquals(payment, refund.getPayment());
        assertEquals("단순 변심", refund.getReason());
        assertEquals(40_000L, refund.getRefundPgAmount());
        assertEquals(10_000L, refund.getRefundPointAmount());
        assertEquals(RefundStatus.REQUESTED, refund.getRefundStatus());
    }

    @Test
    void requestRefundRejectsOtherMemberPayment() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        RefundService refundService = new RefundService(refundRepository, paymentRepository);
        Payment payment = newPaidPayment(1L, 10L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundService.requestRefund(1L, 20L, "단순 변심")
        );

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void requestRefundRejectsUnpaidPayment() {
        RefundRepository refundRepository = mock(RefundRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        RefundService refundService = new RefundService(refundRepository, paymentRepository);
        Payment payment = newPaidPayment(1L, 10L);
        setField(payment, "status", PaymentStatus.PENDING);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundService.requestRefund(1L, 10L, "단순 변심")
        );

        assertEquals(ErrorCode.INVALID_REFUND_STATUS, exception.getErrorCode());
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
