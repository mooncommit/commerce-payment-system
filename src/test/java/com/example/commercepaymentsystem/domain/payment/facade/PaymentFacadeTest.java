package com.example.commercepaymentsystem.domain.payment.facade;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentFacadeTest {

    @Test
    void confirmPaymentDelegatesToPaymentService() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentFacade paymentFacade = new PaymentFacade(paymentService);
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        PaymentConfirmResponse expected = PaymentConfirmResponse.builder()
                .paymentId(1L)
                .build();
        LoginMember loginMember = new LoginMember(10L, "member@example.com");

        when(paymentService.confirmPayment(loginMember.getMemberId(), request)).thenReturn(expected);

        PaymentConfirmResponse actual = paymentFacade.confirmPayment(loginMember, request);

        assertSame(expected, actual);
        verify(paymentService).confirmPayment(loginMember.getMemberId(), request);
    }
}
