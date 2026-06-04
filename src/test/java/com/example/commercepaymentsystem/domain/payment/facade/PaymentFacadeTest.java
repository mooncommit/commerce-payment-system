package com.example.commercepaymentsystem.domain.payment.facade;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGateway;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentFacadeTest {

    @Test
    void confirmPaymentApprovesPaymentWhenPgPaymentIsPaidAndAmountMatches() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        PaymentFacade paymentFacade = new PaymentFacade(paymentService, paymentGateway);
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        PaymentConfirmResponse readyPayment = PaymentConfirmResponse.builder()
                .paymentId(1L)
                .portonePaymentId("pay_test")
                .pgAmount(40_000L)
                .build();
        PaymentConfirmResponse expected = PaymentConfirmResponse.builder()
                .paymentId(1L)
                .paymentStatus(com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus.COMPLETED)
                .build();
        LoginMember loginMember = new LoginMember(10L, "member@example.com");

        when(paymentService.confirmPayment(loginMember.getMemberId(), request)).thenReturn(readyPayment);
        when(paymentGateway.getPayment("pay_test")).thenReturn(new PaymentGatewayResponse("pg_tx_1", "PAID", 40_000));
        when(paymentService.approvePayment(loginMember.getMemberId(), request)).thenReturn(expected);

        PaymentConfirmResponse actual = paymentFacade.confirmPayment(loginMember, request);

        assertSame(expected, actual);
        verify(paymentService).confirmPayment(loginMember.getMemberId(), request);
        verify(paymentGateway).getPayment("pay_test");
        verify(paymentService).approvePayment(loginMember.getMemberId(), request);
    }

    @Test
    void confirmPaymentFailsPaymentWhenPgPaymentIsNotPaid() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        PaymentFacade paymentFacade = new PaymentFacade(paymentService, paymentGateway);
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        LoginMember loginMember = new LoginMember(10L, "member@example.com");
        PaymentConfirmResponse readyPayment = PaymentConfirmResponse.builder()
                .paymentId(1L)
                .portonePaymentId("pay_test")
                .pgAmount(40_000L)
                .build();

        when(paymentService.confirmPayment(loginMember.getMemberId(), request)).thenReturn(readyPayment);
        when(paymentGateway.getPayment("pay_test")).thenReturn(new PaymentGatewayResponse("pg_tx_1", "READY", 40_000));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentFacade.confirmPayment(loginMember, request)
        );

        assertEquals(ErrorCode.PAYMENT_NOT_PAID, exception.getErrorCode());
        verify(paymentService).failPayment(loginMember.getMemberId(), request, "PG 결제가 완료되지 않았습니다.");
    }

    @Test
    void confirmPaymentCancelsPgPaymentAndFailsPaymentWhenAmountMismatches() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        PaymentFacade paymentFacade = new PaymentFacade(paymentService, paymentGateway);
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        LoginMember loginMember = new LoginMember(10L, "member@example.com");
        PaymentConfirmResponse readyPayment = PaymentConfirmResponse.builder()
                .paymentId(1L)
                .portonePaymentId("pay_test")
                .pgAmount(40_000L)
                .build();

        when(paymentService.confirmPayment(loginMember.getMemberId(), request)).thenReturn(readyPayment);
        when(paymentGateway.getPayment("pay_test")).thenReturn(new PaymentGatewayResponse("pg_tx_1", "PAID", 39_000));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentFacade.confirmPayment(loginMember, request)
        );

        assertEquals(ErrorCode.PAYMENT_AMOUNT_MISMATCH, exception.getErrorCode());
        verify(paymentGateway).cancelPayment("pay_test", "결제 금액 불일치 자동 취소");
        verify(paymentService).failPayment(loginMember.getMemberId(), request, "결제 금액이 일치하지 않습니다.");
    }
}
