package com.example.commercepaymentsystem.domain.refund.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.refund.dto.RefundRequest;
import com.example.commercepaymentsystem.domain.refund.dto.RefundResponse;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import com.example.commercepaymentsystem.domain.refund.facade.RefundFacade;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefundControllerTest {

    @Test
    void requestRefundMappingMatchesApiSpec() throws NoSuchMethodException {
        RequestMapping classMapping = RefundController.class.getAnnotation(RequestMapping.class);
        Method requestRefund = RefundController.class.getDeclaredMethod(
                "requestRefund",
                LoginMember.class,
                Long.class,
                RefundRequest.class
        );
        PostMapping postMapping = requestRefund.getAnnotation(PostMapping.class);

        assertEquals("/api/payments/{paymentId}/refunds", classMapping.value()[0]);
        assertEquals(0, postMapping.value().length);
    }

    @Test
    void requestRefundReturnsOkRefundResponse() {
        RefundFacade refundFacade = mock(RefundFacade.class);
        RefundController refundController = new RefundController(refundFacade);
        LoginMember loginMember = new LoginMember(10L, "user@example.com");
        RefundRequest request = new RefundRequest("단순 변심");
        LocalDateTime createdAt = LocalDateTime.parse("2026-06-01T15:30:00");
        LocalDateTime refundedAt = LocalDateTime.parse("2026-06-01T15:30:05");

        RefundResponse refundResponse = RefundResponse.builder()
                .refundId(2L)
                .paymentId(1L)
                .orderId(3L)
                .refundPgAmount(40_000L)
                .refundPointAmount(10_000L)
                .refundStatus(RefundStatus.COMPLETED)
                .orderStatus(OrderStatus.CANCELED)
                .paymentStatus(PaymentStatus.REFUNDED)
                .reason("단순 변심")
                .createdAt(createdAt)
                .refundedAt(refundedAt)
                .build();

        when(refundFacade.requestRefund(loginMember, 1L, request)).thenReturn(refundResponse);

        ResponseEntity<ApiResponse<RefundResponse>> response =
                refundController.requestRefund(loginMember, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("전체 환불 성공", response.getBody().getMessage());
        assertEquals(2L, response.getBody().getData().getRefundId());
        assertEquals(1L, response.getBody().getData().getPaymentId());
        assertEquals(3L, response.getBody().getData().getOrderId());
        assertEquals(40_000L, response.getBody().getData().getRefundPgAmount());
        assertEquals(10_000L, response.getBody().getData().getRefundPointAmount());
        assertEquals("단순 변심", response.getBody().getData().getReason());
        assertEquals(RefundStatus.COMPLETED, response.getBody().getData().getRefundStatus());
        assertEquals(OrderStatus.CANCELED, response.getBody().getData().getOrderStatus());
        assertEquals(PaymentStatus.REFUNDED, response.getBody().getData().getPaymentStatus());
        assertEquals(createdAt, response.getBody().getData().getCreatedAt());
        assertEquals(refundedAt, response.getBody().getData().getRefundedAt());
        verify(refundFacade).requestRefund(loginMember, 1L, request);
    }
}
