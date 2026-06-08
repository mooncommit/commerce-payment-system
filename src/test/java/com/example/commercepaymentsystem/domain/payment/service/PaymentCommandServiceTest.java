package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.enums.SaleStatus;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import com.example.commercepaymentsystem.domain.product.service.ProductService;
import com.example.commercepaymentsystem.domain.point.service.PointService;
import com.example.commercepaymentsystem.domain.refund.service.RefundService;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentCommandServiceTest {

    @Test
    void approvePaymentAndOrderChangesPaymentAndOrderToCompleted() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);
        OrderService orderService = new OrderService(null, null, null, null, null, paymentService, null);
        ProductService productService = new ProductService(productRepository);
        PaymentCommandService commandService = new PaymentCommandService(
                paymentService,
                orderService,
                productService,
                orderItemRepository,
                mock(PointService.class),
                mock(RefundService.class)
        );

        Order order = newEntity(Order.class);
        setField(order, "id", 1L);
        setField(order, "orderNumber", "ORD-TEST-001");
        setField(order, "orderStatus", OrderStatus.PAYMENT_PENDING);
        setField(order, "totalAmount", 50_000L);
        setField(order, "usedPointAmount", 10_000L);
        setField(order, "pgAmount", 40_000L);

        Payment payment = newEntity(Payment.class);
        setField(payment, "id", 1L);
        setField(payment, "order", order);
        setField(payment, "status", PaymentStatus.PENDING);
        setField(payment, "portonePaymentId", "pay_test");

        PaymentConfirmRequest request = newEntity(PaymentConfirmRequest.class);
        setField(request, "paymentId", 1L);
        setField(request, "portonePaymentId", "pay_test");

        when(paymentRepository.findByIdAndOrder_Member_Id(1L, 10L)).thenReturn(Optional.of(payment));

        PaymentConfirmResponse response = commandService.approvePaymentAndOrder(10L, request);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getOrderStatus());
        assertEquals(1L, response.getPaymentId());
    }

    @Test
    void failPaymentAndOrderChangesPaymentAndOrderToCanceledAndRestoresStock() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);
        OrderService orderService = new OrderService(null, null, null, null, null, paymentService, null);
        ProductService productService = new ProductService(productRepository);
        PaymentCommandService commandService = new PaymentCommandService(
                paymentService,
                orderService,
                productService,
                orderItemRepository,
                mock(PointService.class),
                mock(RefundService.class)
        );

        Order order = newEntity(Order.class);
        setField(order, "id", 1L);
        setField(order, "orderStatus", OrderStatus.PAYMENT_PENDING);

        Payment payment = newEntity(Payment.class);
        setField(payment, "id", 1L);
        setField(payment, "order", order);
        setField(payment, "status", PaymentStatus.PENDING);
        setField(payment, "portonePaymentId", "pay_test");

        PaymentConfirmRequest request = newEntity(PaymentConfirmRequest.class);
        setField(request, "paymentId", 1L);
        setField(request, "portonePaymentId", "pay_test");

        Product product = newEntity(Product.class);
        setField(product, "id", 100L);
        setField(product, "stockQuantity", 5);
        setField(product, "saleStatus", SaleStatus.ON_SALE);

        OrderItem orderItem = newEntity(OrderItem.class);
        setField(orderItem, "product", product);
        setField(orderItem, "quantity", 2);

        when(paymentRepository.findByIdAndOrder_Member_Id(1L, 10L)).thenReturn(Optional.of(payment));
        when(orderItemRepository.findAllByOrder_Id(1L)).thenReturn(List.of(orderItem));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        commandService.failPaymentAndOrder(10L, request, "PG 결제가 완료되지 않았습니다.");

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
        assertEquals(7, product.getStockQuantity());
    }

    @Test
    void refundPaymentAndOrderChangesPaymentAndOrderRestoresStockAndCompletesRefund() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);
        OrderService orderService = new OrderService(null, null, null, null, null, paymentService, null);
        ProductService productService = new ProductService(productRepository);
        PointService pointService = mock(PointService.class);
        RefundService refundService = mock(RefundService.class);
        PaymentCommandService commandService = new PaymentCommandService(
                paymentService,
                orderService,
                productService,
                orderItemRepository,
                pointService,
                refundService
        );

        Order order = newEntity(Order.class);
        setField(order, "id", 1L);
        setField(order, "orderStatus", OrderStatus.COMPLETED);

        Payment payment = newEntity(Payment.class);
        setField(payment, "id", 1L);
        setField(payment, "order", order);
        setField(payment, "status", PaymentStatus.COMPLETED);
        setField(payment, "portonePaymentId", "pay_test");

        Product product = newEntity(Product.class);
        setField(product, "id", 100L);
        setField(product, "stockQuantity", 5);
        setField(product, "saleStatus", SaleStatus.ON_SALE);

        OrderItem orderItem = newEntity(OrderItem.class);
        setField(orderItem, "product", product);
        setField(orderItem, "quantity", 2);

        Refund refund = mock(Refund.class);
        when(paymentRepository.findByIdWithOrder(1L)).thenReturn(Optional.of(payment));
        when(orderItemRepository.findAllByOrder_Id(1L)).thenReturn(List.of(orderItem));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(refundService.markCompleted(2L)).thenReturn(refund);

        Refund result = commandService.refundPaymentAndOrder(1L, 2L);

        assertEquals(refund, result);
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
        assertEquals(7, product.getStockQuantity());
        verify(refundService).markCompleted(2L);
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
