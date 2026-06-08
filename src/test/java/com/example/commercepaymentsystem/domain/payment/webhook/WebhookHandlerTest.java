package com.example.commercepaymentsystem.domain.payment.webhook;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGateway;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;
import com.example.commercepaymentsystem.domain.payment.service.PaymentCommandService;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.domain.payment.webhook.entity.WebhookEvent;
import com.example.commercepaymentsystem.domain.payment.webhook.service.WebhookEventService;
import io.portone.sdk.server.webhook.WebhookTransactionCancelledCancelled;
import io.portone.sdk.server.webhook.WebhookTransactionCancelledDataCancelled;
import io.portone.sdk.server.webhook.WebhookTransactionDataPaid;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookHandlerTest {

    @Test
    void paidWebhookProcessesPendingPaymentWhenPgAmountMatches() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        WebhookEventService webhookEventService = mock(WebhookEventService.class);
        WebhookHandler handler = new WebhookHandler(
                paymentService,
                paymentCommandService,
                paymentGateway,
                webhookEventService
        );
        WebhookEvent event = webhookEvent(1L);
        Payment payment = pendingPaymentWithPgAmount(40_000L);
        WebhookTransactionPaid webhook = new WebhookTransactionPaid(
                Instant.parse("2026-06-06T00:00:00Z"),
                new WebhookTransactionDataPaid("pay_test", "store_test", "tx_test")
        );

        when(webhookEventService.saveIfNotDuplicate("webhook_1", "WebhookTransactionPaid", "{}"))
                .thenReturn(Optional.of(event));
        when(paymentGateway.getPayment("pay_test"))
                .thenReturn(new PaymentGatewayResponse("tx_test", "PAID", 40_000L));
        when(paymentService.findByPortonePaymentId("pay_test")).thenReturn(payment);

        handler.handle("webhook_1", webhook, "{}");

        verify(paymentCommandService).processPaidWebhook("pay_test");
        verify(webhookEventService).markProcessed(1L);
    }

    @Test
    void paidWebhookMarksFailedWhenPgAmountDoesNotMatch() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        WebhookEventService webhookEventService = mock(WebhookEventService.class);
        WebhookHandler handler = new WebhookHandler(
                paymentService,
                paymentCommandService,
                paymentGateway,
                webhookEventService
        );
        WebhookEvent event = webhookEvent(2L);
        Payment payment = pendingPaymentWithPgAmount(40_000L);
        WebhookTransactionPaid webhook = new WebhookTransactionPaid(
                Instant.parse("2026-06-06T00:00:00Z"),
                new WebhookTransactionDataPaid("pay_test", "store_test", "tx_test")
        );

        when(webhookEventService.saveIfNotDuplicate("webhook_2", "WebhookTransactionPaid", "{}"))
                .thenReturn(Optional.of(event));
        when(paymentGateway.getPayment("pay_test"))
                .thenReturn(new PaymentGatewayResponse("tx_test", "PAID", 39_000L));
        when(paymentService.findByPortonePaymentId("pay_test")).thenReturn(payment);

        handler.handle("webhook_2", webhook, "{}");

        verify(webhookEventService).markFailed(2L, "금액 불일치: db=40000, pg=39000");
        verify(paymentCommandService, never()).processPaidWebhook("pay_test");
    }

    @Test
    void cancelledWebhookIsRecordedAsIgnored() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentCommandService paymentCommandService = mock(PaymentCommandService.class);
        PaymentGateway paymentGateway = mock(PaymentGateway.class);
        WebhookEventService webhookEventService = mock(WebhookEventService.class);
        WebhookHandler handler = new WebhookHandler(
                paymentService,
                paymentCommandService,
                paymentGateway,
                webhookEventService
        );
        WebhookEvent event = webhookEvent(3L);
        WebhookTransactionCancelledCancelled webhook = new WebhookTransactionCancelledCancelled(
                Instant.parse("2026-06-06T00:00:00Z"),
                new WebhookTransactionCancelledDataCancelled("pay_test", "store_test", "tx_test", "cancel_test")
        );

        when(webhookEventService.saveIfNotDuplicate("webhook_3", "WebhookTransactionCancelledCancelled", "{}"))
                .thenReturn(Optional.of(event));
        when(paymentGateway.getPayment("pay_test"))
                .thenReturn(new PaymentGatewayResponse("tx_test", "CANCELLED", 40_000L));

        handler.handle("webhook_3", webhook, "{}");

        verify(webhookEventService).markIgnored(3L, "취소 웹훅은 현재 자동 반영하지 않음: pay_test");
        verify(paymentCommandService, never()).processPaidWebhook("pay_test");
    }

    private static Payment pendingPaymentWithPgAmount(Long pgAmount) {
        Order order = newEntity(Order.class);
        setField(order, "pgAmount", pgAmount);

        Payment payment = newEntity(Payment.class);
        setField(payment, "order", order);
        setField(payment, "status", PaymentStatus.PENDING);
        setField(payment, "portonePaymentId", "pay_test");
        return payment;
    }

    private static WebhookEvent webhookEvent(Long id) {
        WebhookEvent event = new WebhookEvent("webhook_" + id, "type", "{}");
        setField(event, "id", id);
        return event;
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
