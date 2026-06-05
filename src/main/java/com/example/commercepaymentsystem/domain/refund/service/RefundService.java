package com.example.commercepaymentsystem.domain.refund.service;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.payment.enums.PaymentStatus;
import com.example.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
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
    private final PaymentRepository paymentRepository;

    @Transactional
    public Refund requestRefund(Long paymentId, Long memberId, String cancelReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_STATUS);
        }

        return createRefund(payment, cancelReason);
    }

    @Transactional
    public Refund createRefund(Payment payment, String cancelReason) {
        Refund refund = new Refund(payment, cancelReason);
        return refundRepository.save(refund);
    }

}
