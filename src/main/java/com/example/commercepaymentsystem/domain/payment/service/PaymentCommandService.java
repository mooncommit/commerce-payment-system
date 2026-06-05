package com.example.commercepaymentsystem.domain.payment.service;

import com.example.commercepaymentsystem.domain.order.entity.Order;
import com.example.commercepaymentsystem.domain.order.entity.OrderItem;
import com.example.commercepaymentsystem.domain.order.repository.OrderItemRepository;
import com.example.commercepaymentsystem.domain.order.service.OrderService;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.product.entity.Product;
import com.example.commercepaymentsystem.domain.product.service.ProductService;
import com.example.commercepaymentsystem.domain.point.service.PointService;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.service.RefundService;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 승인과 실패 시 여러 도메인(결제, 주문, 상품)을 한 트랜잭션으로 묶어 처리하는 유스케이스 서비스.
 *
 * <p>개별 서비스는 자기 도메인만 책임지고, 이 클래스는 그 조각들을 순서대로 조립한다.
 * 결제 상태 변경, 주문 상태 변경, 재고 복구처럼 한 흐름으로 함께 처리되어야 하는 작업을
 * 하나의 트랜잭션 안에서 묶는다.
 */
@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final OrderItemRepository orderItemRepository;
    private final PointService pointService;
    private final RefundService refundService;

    /**
     * 결제 승인 후 결제와 주문 상태를 함께 확정한다.
     *
     * @param memberId 요청한 회원 ID
     * @param request 결제 승인 요청 정보
     * @return 결제 확정 결과 응답
     */
    @Transactional
    public PaymentConfirmResponse approvePaymentAndOrder(Long memberId, PaymentConfirmRequest request) {
        Payment payment = paymentService.findReadyPayment(memberId, request);
        validatePending(payment);

        paymentService.markPaid(payment);
        orderService.confirmOrder(payment.getOrder());

        return paymentService.toConfirmResponse(payment);
    }

    /**
     * 결제 실패 시 결제와 주문을 함께 취소하고, 주문에 연결된 재고를 복구한다.
     *
     * @param memberId 요청한 회원 ID
     * @param request 결제 실패 요청 정보
     * @param failureReason 실패 사유
     */
    @Transactional
    public void failPaymentAndOrder(Long memberId, PaymentConfirmRequest request, String failureReason) {
        Payment payment = paymentService.findReadyPayment(memberId, request);
        validatePending(payment);

        paymentService.markFailed(payment, failureReason);
        orderService.cancelOrder(payment.getOrder());
        restoreStock(payment.getOrder());
    }

    @Transactional
    public Refund refundPaymentAndOrder(Long paymentId, String reason) {
        Payment payment = paymentService.findByIdWithOrder(paymentId);
        validateCompleted(payment);

        payment.markRefunded();
        orderService.cancelOrder(payment.getOrder());
        restoreStock(payment.getOrder());
        pointService.restoreUsedPoints(payment);
        pointService.revokeEarnedPoints(payment);

        return refundService.createRefund(payment, reason);
    }

    /**
     * 주문 생성 시 차감한 수량을 상품 엔티티에 다시 더한다.
     *
     * @param order 재고를 복구할 주문
     */
    private void restoreStock(Order order) {
        for (OrderItem orderItem : orderItemRepository.findAllByOrder_Id(order.getId())) {
            Product product = productService.findProductEntity(orderItem.getProduct().getId());
            productService.restoreStock(product, orderItem.getQuantity());
        }
    }

    /**
     * 이미 처리된 결제는 승인/실패 흐름에서 다시 건드리지 못하게 막는다.
     *
     * @param payment 검증할 결제
     */
    private void validatePending(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }
    }

    private void validateCompleted(Payment payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_STATUS);
        }
    }
}
