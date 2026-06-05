package com.example.commercepaymentsystem.domain.refund.service;

import com.example.commercepaymentsystem.domain.payment.entity.Payment;
import com.example.commercepaymentsystem.domain.refund.entity.Refund;
import com.example.commercepaymentsystem.domain.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;

    @Transactional
    public Refund createRefund(Payment payment, String cancelReason) {
        Refund refund = Refund.builder()
                .payment(payment)
                .refundPgAmount(payment.getOrder().getPgAmount())
                .refundPointAmount(payment.getOrder().getUsedPointAmount())
                .reason(cancelReason)
                .build();
        return refundRepository.save(refund);
    }

}
