package com.example.commercepaymentsystem.domain.refund.facade;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentMethodType;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGateway;
import com.example.commercepaymentsystem.domain.payment.service.PaymentCommandService;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.domain.refund.dto.RefundRequest;
import com.example.commercepaymentsystem.domain.refund.dto.RefundResponse;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import com.example.commercepaymentsystem.domain.refund.service.RefundService;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefundFacadeTest {

    @Test
    void requestRefundCompletesInternalStateChangesBeforePgCancel() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
        RefundService refundService = mock(RefundService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        RefundFacade refundFacade = new RefundFacade(paymentService, paymentCommandService, refundService, paymentGateway);
        Payment payment = completedPayment(1L, 10L, "pay_test");
        Refund requestedRefund = refund(2L, payment, "단순 변심");
        Refund completedRefund = refund(2L, payment, "단순 변심");
        completedRefund.markAsCompleted();
        LoginMember loginMember = new LoginMember(10L, "member@example.com");

        when(paymentService.findByIdWithOrder(1L)).thenReturn(payment);
        when(refundService.createRefund(payment, "단순 변심")).thenReturn(requestedRefund);
        when(paymentCommandService.refundPaymentAndOrder(1L, 2L)).thenReturn(completedRefund);

        RefundResponse response = refundFacade.requestRefund(loginMember, 1L, new RefundRequest("단순 변심"));

        assertEquals(1L, response.getPaymentId());
        assertEquals(RefundStatus.COMPLETED, response.getRefundStatus());
        InOrder inOrder = inOrder(refundService, paymentCommandService, paymentGateway);
        inOrder.verify(refundService).createRefund(payment, "단순 변심");
        inOrder.verify(paymentCommandService).refundPaymentAndOrder(1L, 2L);
        inOrder.verify(paymentGateway).cancelPayment("pay_test", "단순 변심");
    }

    @Test
    void requestRefundKeepsCompletedInternalStateWhenPgCancelFailsAfterCommit() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
        RefundService refundService = mock(RefundService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        RefundFacade refundFacade = new RefundFacade(paymentService, paymentCommandService, refundService, paymentGateway);
        Payment payment = completedPayment(1L, 10L, "pay_test");
        Refund requestedRefund = refund(2L, payment, "단순 변심");
        Refund completedRefund = refund(2L, payment, "단순 변심");
        completedRefund.markAsCompleted();
        LoginMember loginMember = new LoginMember(10L, "member@example.com");

        when(paymentService.findByIdWithOrder(1L)).thenReturn(payment);
        when(refundService.createRefund(payment, "단순 변심")).thenReturn(requestedRefund);
        when(paymentCommandService.refundPaymentAndOrder(1L, 2L)).thenReturn(completedRefund);
        doThrow(new RuntimeException("cancel failed"))
                .when(paymentGateway)
                .cancelPayment("pay_test", "단순 변심");

        RefundResponse response = refundFacade.requestRefund(loginMember, 1L, new RefundRequest("단순 변심"));

        assertEquals(RefundStatus.COMPLETED, response.getRefundStatus());
        InOrder inOrder = inOrder(refundService, paymentCommandService, paymentGateway);
        inOrder.verify(refundService).createRefund(payment, "단순 변심");
        inOrder.verify(paymentCommandService).refundPaymentAndOrder(1L, 2L);
        inOrder.verify(paymentGateway).cancelPayment("pay_test", "단순 변심");
        verify(refundService, never()).markFailed(2L);
    }

    @Test
    void requestRefundSkipsPgCancelForPointOnlyPaymentAndCompletesInternalStateChanges() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
        RefundService refundService = mock(RefundService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        RefundFacade refundFacade = new RefundFacade(paymentService, paymentCommandService, refundService, paymentGateway);
        Payment payment = completedPayment(1L, 10L, "pay_test");
        setField(payment, "paymentMethodType", PaymentMethodType.POINT_ONLY);
        Refund requestedRefund = refund(2L, payment, "단순 변심");
        Refund completedRefund = refund(2L, payment, "단순 변심");
        completedRefund.markAsCompleted();
        LoginMember loginMember = new LoginMember(10L, "member@example.com");

        when(paymentService.findByIdWithOrder(1L)).thenReturn(payment);
        when(refundService.createRefund(payment, "단순 변심")).thenReturn(requestedRefund);
        when(paymentCommandService.refundPaymentAndOrder(1L, 2L)).thenReturn(completedRefund);

        RefundResponse response = refundFacade.requestRefund(loginMember, 1L, new RefundRequest("단순 변심"));

        assertEquals(RefundStatus.COMPLETED, response.getRefundStatus());
        verify(paymentGateway, never()).cancelPayment("pay_test", "단순 변심");
        verify(paymentCommandService).refundPaymentAndOrder(1L, 2L);
    }

    @Test
    void requestRefundRejectsOtherMemberPaymentBeforeInternalTransactionAndPgCancel() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
        RefundService refundService = mock(RefundService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        RefundFacade refundFacade = new RefundFacade(paymentService, paymentCommandService, refundService, paymentGateway);
        Payment payment = completedPayment(1L, 10L, "pay_test");
        LoginMember loginMember = new LoginMember(20L, "member@example.com");

        when(paymentService.findByIdWithOrder(1L)).thenReturn(payment);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundFacade.requestRefund(loginMember, 1L, new RefundRequest("단순 변심"))
        );

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        verify(refundService, never()).createRefund(payment, "단순 변심");
        verify(paymentCommandService, never()).refundPaymentAndOrder(1L, 2L);
        verify(paymentGateway, never()).cancelPayment("pay_test", "단순 변심");
    }

    @Test
    void requestRefundRejectsNotCompletedPaymentBeforeInternalTransactionAndPgCancel() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
        RefundService refundService = mock(RefundService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        RefundFacade refundFacade = new RefundFacade(paymentService, paymentCommandService, refundService, paymentGateway);
        Payment payment = completedPayment(1L, 10L, "pay_test");
        setField(payment, "status", PaymentStatus.PENDING);
        LoginMember loginMember = new LoginMember(10L, "member@example.com");

        when(paymentService.findByIdWithOrder(1L)).thenReturn(payment);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundFacade.requestRefund(loginMember, 1L, new RefundRequest("단순 변심"))
        );

        assertEquals(ErrorCode.INVALID_REFUND_STATUS, exception.getErrorCode());
        verify(refundService, never()).createRefund(payment, "단순 변심");
        verify(paymentCommandService, never()).refundPaymentAndOrder(1L, 2L);
        verify(paymentGateway, never()).cancelPayment("pay_test", "단순 변심");
    }

    private static Payment completedPayment(Long paymentId, Long memberId, String portonePaymentId) {
        Member member = newEntity(Member.class);
        setField(member, "id", memberId);

        Order order = newEntity(Order.class);
        setField(order, "member", member);
        setField(order, "pgAmount", 40_000L);
        setField(order, "usedPointAmount", 10_000L);

        Payment payment = newEntity(Payment.class);
        setField(payment, "id", paymentId);
        setField(payment, "order", order);
        setField(payment, "portonePaymentId", portonePaymentId);
        setField(payment, "status", PaymentStatus.COMPLETED);
        setField(payment, "paymentMethodType", PaymentMethodType.CARD);
        return payment;
    }

    private static Refund refund(Long refundId, Payment payment, String reason) {
        Refund refund = Refund.builder()
                .payment(payment)
                .refundPgAmount(40_000L)
                .refundPointAmount(10_000L)
                .reason(reason)
                .build();
        setField(refund, "id", refundId);
        return refund;
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
