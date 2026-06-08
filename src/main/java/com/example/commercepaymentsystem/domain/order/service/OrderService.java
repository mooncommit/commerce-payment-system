package com.example.commercepaymentsystem.domain.order.service;

import com.example.commercepaymentsystem.domain.cart.entity.CartItem;
import com.example.commercepaymentsystem.domain.cart.repository.CartItemRepository;
import com.example.commercepaymentsystem.domain.member.entity.Member;
import com.example.commercepaymentsystem.domain.member.repository.MemberRepository;
import com.example.commercepaymentsystem.domain.order.dto.CartOrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCancelItemResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderCancelResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import com.example.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderDetailItemResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderDetailResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderItemResponse;
import com.example.commercepaymentsystem.domain.order.dto.OrderListResponse;
import com.example.commercepaymentsystem.domain.order.dto.PreviewOrderItemResponse;
import com.example.commercepaymentsystem.domain.order.dto.PreviewOrderResponse;
import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.enums.OrderStatus;
import com.example.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import com.example.commercepaymentsystem.domain.order.repository.OrderRepository;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentMethodType;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.repository.ProductRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import com.example.commercepaymentsystem.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // PG 결제 금액의 1% 적립
    private static final int EARN_POINT_RATE = 100;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public void confirmOrder(Order order) {
        order.markAsConfirmed();
    }

    public void cancelOrder(Order order) {
        order.markAsCancelled();
    }

    // 내 주문 내역 조회
    public PageResponse<OrderListResponse> getOrders(Long memberId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Order> orderPage = orderRepository.findByMemberId(memberId, pageable);

        List<OrderListResponse> content = orderPage.getContent()
                .stream()
                .map(OrderListResponse::from)
                .toList();

        return new PageResponse<>(
                content,
                orderPage.getNumber() + 1,
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    // 주문서 미리보기
    public PreviewOrderResponse previewOrder(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        List<CartItem> cartItems = cartItemRepository.findAllByMemberIdWithProduct(memberId);

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        List<PreviewOrderItemResponse> items = cartItems.stream()
                .map(PreviewOrderItemResponse::from)
                .toList();

        return PreviewOrderResponse.from(items, member.getPointBalance());
    }

    // 내 주문 상세 조회
    public OrderDetailResponse getOrderDetail(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderOwner(order, memberId);

        Payment payment = paymentRepository.findByOrderIdWithOrder(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        List<OrderDetailItemResponse> items = orderItemRepository.findAllByOrder_Id(orderId)
                .stream()
                .map(OrderDetailItemResponse::from)
                .toList();

        return OrderDetailResponse.from(order, payment, items);
    }

    // 결제대기 주문 취소
    @Transactional
    public OrderCancelResponse cancelPendingOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderOwner(order, memberId);
        validateCancelableOrder(order);

        Payment payment = paymentRepository.findByOrderIdWithOrder(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder_Id(orderId);

        paymentService.markFailed(payment, "결제대기 주문 취소");
        order.markAsCancelled();
        restoreStock(orderItems);

        List<OrderCancelItemResponse> restoredItems = orderItems.stream()
                .map(OrderCancelItemResponse::from)
                .toList();

        return OrderCancelResponse.from(order, payment, restoredItems);
    }

    // 상품 바로 주문 생성
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
        Long earnedPointAmount = calculateEarnedPointAmount(pgAmount);

        // 아직 결제되지 않은 주문 먼저 저장
        Order order = Order.createPending(generateOrderNumber(), member, totalAmount, usedPointAmount, pgAmount, earnedPointAmount);
        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = OrderItem.create(savedOrder, product, request.getQuantity());
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        // 주문 생성 시점에 재고 먼저 차감
        product.decreaseStock(request.getQuantity());

        // 결제 대기 데이터는 결제 담당 서비스에 요청
        Payment payment = paymentService.createPendingPayment(savedOrder, PaymentMethodType.CARD);

        return OrderCreateResponse.from(
                savedOrder,
                payment,
                List.of(OrderItemResponse.from(savedOrderItem))
        );
    }

    // 장바구니 주문 생성
    @Transactional
    public OrderCreateResponse createCartOrder(Long memberId, CartOrderCreateRequest request) {
        // 장바구니 상품 기준으로 주문, 주문상품, 재고 차감, 결제 대기 생성을 한 번에 진행
        validateCartOrderRequest(request);

        // 로그인 회원과 주문할 장바구니 상품 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        List<CartItem> cartItems = getCartItemsForOrder(memberId, request.getCartItemIds());

        // 주문 총액과 카드로 결제할 금액 계산
        Long usedPointAmount = request.getUsePointAmount();
        Long totalAmount = calculateTotalAmount(cartItems);
        validatePoint(member, usedPointAmount, totalAmount);

        Long pgAmount = totalAmount - usedPointAmount;
        Long earnedPointAmount = calculateEarnedPointAmount(pgAmount);

        // 아직 결제되지 않은 주문 먼저 저장
        Order order = Order.createPending(generateOrderNumber(), member, totalAmount, usedPointAmount, pgAmount, earnedPointAmount);
        Order savedOrder = orderRepository.save(order);

        // 장바구니 상품을 주문상품으로 옮기고 재고 먼저 차감
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer quantity = cartItem.getQuantity();

            validateProduct(product, quantity);

            orderItems.add(OrderItem.create(savedOrder, product, quantity));
            product.decreaseStock(quantity);
        }

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

        // 결제 대기 데이터는 결제 담당 서비스에 요청
        Payment payment = paymentService.createPendingPayment(savedOrder, PaymentMethodType.CARD);

        return OrderCreateResponse.from(
                savedOrder,
                payment,
                savedOrderItems.stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }

    private void validateCartOrderRequest(CartOrderCreateRequest request) {
        // 주문할 장바구니 상품 ID가 없으면 주문 생성 중단
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }
        if (request.getUsePointAmount() == null || request.getUsePointAmount() < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        Set<Long> cartItemIds = new LinkedHashSet<>(request.getCartItemIds());
        if (cartItemIds.size() != request.getCartItemIds().size()) {
            throw new BusinessException(ErrorCode.INVALID_CART_ITEM_ID);
        }
        if (cartItemIds.stream().anyMatch(cartItemId -> cartItemId == null || cartItemId <= 0)) {
            throw new BusinessException(ErrorCode.INVALID_CART_ITEM_ID);
        }
    }

    private List<CartItem> getCartItemsForOrder(Long memberId, List<Long> cartItemIds) {
        // 요청한 장바구니 상품이 실제로 존재하고 내 상품인지 확인
        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
        if (cartItems.size() != cartItemIds.size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        if (cartItems.stream().anyMatch(cartItem -> !cartItem.getCart().getMember().getId().equals(memberId))) {
            throw new BusinessException(ErrorCode.FORBIDDEN_CART_ITEM);
        }
        return cartItems;
    }

    private Long calculateEarnedPointAmount(Long pgAmount) {
        return pgAmount / EARN_POINT_RATE;
    }

    private Long calculateTotalAmount(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToLong(cartItem -> cartItem.getProduct().getPrice() * cartItem.getQuantity())
                .sum();
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

    private void validateOrderOwner(Order order, Long memberId) {
        // 본인 주문만 조회하거나 취소 가능
        if (!order.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ORDER);
        }
    }

    private void validateCancelableOrder(Order order) {
        // 이미 취소된 주문은 다시 취소 불가
        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELED);
        }

        // 결제대기 상태가 아닌 주문은 주문 취소 API로 취소 불가
        if (order.getOrderStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELABLE);
        }
    }

    private void restoreStock(List<OrderItem> orderItems) {
        // 주문 생성 때 먼저 줄였던 재고를 다시 복구
        for (OrderItem orderItem : orderItems) {
            orderItem.getProduct().restoreStock(orderItem.getQuantity());
        }
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(ORDER_NUMBER_DATE_FORMAT);
        String random = UUID.randomUUID().toString().substring(0, 8);
        return "ORD-" + date + "-" + random;
    }
}
