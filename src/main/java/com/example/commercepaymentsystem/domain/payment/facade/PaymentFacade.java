package com.example.commercepaymentsystem.domain.payment.facade;

import com.example.commercepaymentsystem.domain.auth.dto.LoginMember;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystem.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 결제 승인 유스케이스 Facade
 * 결제 확정은 생성된 Payment를 기준으로 처리한다.
 * 현재 단계에서는 결제 확정 요청 검증과 응답 생성을 PaymentService에 위임한다.
 */
@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;

    public PaymentConfirmResponse confirmPayment(LoginMember loginMember, PaymentConfirmRequest request) {
        return paymentService.confirmPayment(loginMember.getMemberId(), request);
    }
}
