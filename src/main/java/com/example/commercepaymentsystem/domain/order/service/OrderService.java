package com.example.commercepaymentsystem.domain.order.service;

import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderItemResponse;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import com.example.commercepaymentsystem.domain.order.repository.OrderRepository;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentMethodType;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final PaymentService paymentService;

    public void confirmOrder(Order order) {
        order.markAsConfirmed();
    }

    public void cancelOrder(Order order) {
        order.markAsCancelled();
    }

    @Transactional
    public OrderCreateResponse createDirectOrder(Long memberId, OrderCreateRequest request) {
        // 주문, 주문상품, 재고 차감, 결제 대기 생성을 한 번에 진행
        validateRequest(request);

        // 로그인 회원과 주문할 상품 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        validateProduct(product, request.getQuantity());

        // 주문 총액과 카드로 결제할 금액 계산
        Long usedPointAmount = request.getUsePointAmount();
        Long totalAmount = product.getPrice() * request.getQuantity();
        validatePoint(member, usedPointAmount, totalAmount);

        Long pgAmount = totalAmount - usedPointAmount;

        // 아직 결제되지 않은 주문 먼저 저장
        Order order = Order.createPending(generateOrderNumber(), member, totalAmount, usedPointAmount, pgAmount);
        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = OrderItem.create(savedOrder, product, request.getQuantity());
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        // 주문 생성 시점에 재고 먼저 차감
        product.decreaseStock(request.getQuantity());

        // 결제 대기 생성은 결제 담당 서비스에서 처리
        Payment payment = paymentService.createPendingPayment(savedOrder, PaymentMethodType.CARD);

        return new OrderCreateResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                payment.getId(),
                payment.getPortonePaymentId(),
                savedOrder.getOrderStatus(),
                payment.getStatus().name(),
                savedOrder.getTotalAmount(),
                savedOrder.getUsedPointAmount(),
                savedOrder.getPgAmount(),
                List.of(toOrderItemResponse(savedOrderItem))
        );
    }

    private void validateRequest(OrderCreateRequest request) {
        // 요청값이 비어 있거나 잘못된 값이면 주문 생성 중단
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_ID);
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }
        if (request.getUsePointAmount() == null || request.getUsePointAmount() < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }
    }

    private void validateProduct(Product product, Integer quantity) {
        // 판매 중이고 재고가 충분한 상품만 주문 가능
        if (!product.isOnSale()) {
            throw new BusinessException(ErrorCode.PRODUCT_UNAVAILABLE);
        }
        if (!product.hasStock(quantity)) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
    }

    private void validatePoint(Member member, Long usedPointAmount, Long totalAmount) {
        // 주문 금액보다 많거나 가진 포인트보다 많은 포인트 사용 불가
        if (usedPointAmount > totalAmount) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }
        if (member.getPointBalance() < usedPointAmount) {
            throw new BusinessException(ErrorCode.POINT_BALANCE_NOT_ENOUGH);
        }
    }

    private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProductName(),
                orderItem.getUnitPrice(),
                orderItem.getQuantity(),
                orderItem.getLineTotalAmount()
        );
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(ORDER_NUMBER_DATE_FORMAT);
        String random = UUID.randomUUID().toString().substring(0, 8);
        return "ORD-" + date + "-" + random;
    }
}
