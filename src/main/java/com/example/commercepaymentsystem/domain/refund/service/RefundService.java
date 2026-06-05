package com.example.commercepaymentsystem.domain.refund.service;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.enums.RefundStatus;
import com.example.commercepaymentsystem.domain.refund.repository.RefundRepository;
import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;

    /**
     * 환불 시도 이력을 REQUESTED 상태로 먼저 생성한다.
     *
     * <p>아직 PG 취소, 결제 상태 변경, 재고/포인트 복구는 수행하지 않는다.
     * 이 단계에서 Refund를 남겨두면 이후 PG 통신 실패나 후처리 실패가 발생해도
     * 어떤 결제에 대해 환불을 시도했는지 추적할 수 있다.
     * 이미 FAILED로 남은 환불 이력이 있다면 같은 row를 REQUESTED로 되돌려 재시도한다.
     *
     * @param payment 환불 대상 결제
     * @param cancelReason 환불 사유
     * @return REQUESTED 상태로 저장된 환불 이력
     */
    @Transactional
    public Refund createRefund(Payment payment, String cancelReason) {
        return refundRepository.findByPayment_Id(payment.getId())
                .map(refund -> retryRefund(refund, cancelReason))
                .orElseGet(() -> saveNewRefund(payment, cancelReason));
    }

    private Refund retryRefund(Refund refund, String cancelReason) {
        if (refund.getRefundStatus() == RefundStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_STATUS);
        }
        refund.markAsRequested(cancelReason);
        return refund;
    }

    private Refund saveNewRefund(Payment payment, String cancelReason) {
        Refund refund = Refund.builder()
                .payment(payment)
                .refundPgAmount(payment.getOrder().getPgAmount())
                .refundPointAmount(payment.getOrder().getUsedPointAmount())
                .reason(cancelReason)
                .build();
        return refundRepository.save(refund);
    }

    /**
     * PG 취소와 내부 후처리가 모두 성공한 환불 이력을 COMPLETED 상태로 확정한다.
     *
     * @param refundId 완료 처리할 환불 ID
     * @return COMPLETED 상태로 변경된 환불 이력
     */
    @Transactional
    public Refund markCompleted(Long refundId) {
        Refund refund = findById(refundId);
        refund.markAsCompleted();
        return refund;
    }

    /**
     * PG 취소가 실패한 환불 이력을 FAILED 상태로 남긴다.
     *
     * <p>이 경우 실제 결제 취소가 완료되지 않았으므로 Payment와 Order 상태는 변경하지 않는다.
     * 운영자는 FAILED 환불 이력을 기준으로 PG 통신 실패 건을 확인하고 후속 처리할 수 있다.
     *
     * @param refundId 실패 처리할 환불 ID
     */
    @Transactional
    public void markFailed(Long refundId) {
        Refund refund = findById(refundId);
        refund.markAsFailed();
    }

    private Refund findById(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));
    }

}
