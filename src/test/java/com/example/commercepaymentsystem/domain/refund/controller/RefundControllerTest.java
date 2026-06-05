package com.example.commercepaymentsystem.domain.refund.controller;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.refund.dto.RefundRequest;
import com.example.commercepaymentsystem.domain.refund.dto.RefundResponse;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import com.example.commercepaymentsystem.domain.refund.service.RefundService;
import com.example.commercepaymentsystem.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    void requestRefundReturnsCreatedRefundResponse() {
        RefundService refundService = mock(RefundService.class);
        RefundController refundController = new RefundController(refundService);
        LoginMember loginMember = new LoginMember(10L, "user@example.com");
        RefundRequest request = new RefundRequest("단순 변심");

        Payment payment = newEntity(Payment.class);
        setField(payment, "id", 1L);
        Refund refund = Refund.builder()
                .payment(payment)
                .refundPgAmount(40_000L)
                .refundPointAmount(10_000L)
                .reason("단순 변심")
                .build();
        setField(refund, "id", 2L);

        when(refundService.requestRefund(1L, 10L, "단순 변심")).thenReturn(refund);

        ResponseEntity<ApiResponse<RefundResponse>> response =
                refundController.requestRefund(loginMember, 1L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals(2L, response.getBody().getData().getRefundId());
        assertEquals(1L, response.getBody().getData().getPaymentId());
        assertEquals(40_000L, response.getBody().getData().getRefundPgAmount());
        assertEquals(10_000L, response.getBody().getData().getRefundPointAmount());
        assertEquals("단순 변심", response.getBody().getData().getReason());
        assertEquals(RefundStatus.REQUESTED, response.getBody().getData().getRefundStatus());
        verify(refundService).requestRefund(1L, 10L, "단순 변심");
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
