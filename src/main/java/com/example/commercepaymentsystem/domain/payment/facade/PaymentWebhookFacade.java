package com.example.commercepaymentsystem.domain.payment.facade;

import com.example.commercepaymentsystem.domain.payment.dto.WebhookPayload;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGateway;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;
import com.example.commercepaymentsystem.domain.payment.service.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookFacade {

    private final PaymentGateway paymentGateway;
    private final PaymentCommandService paymentCommandService;

    public void processWebhook(WebhookPayload payload) {
        String impUid = payload.getImp_uid();
        log.info("웹훅 수신: imp_uid={}, status={}", impUid, payload.getStatus());

        try {
            // PG사에서 실제 결제 정보를 조회하여 위변조 여부 교차 검증
            PaymentGatewayResponse pgResponse = paymentGateway.getPayment(impUid);
            String pgStatus = pgResponse.status();

            if ("PAID".equalsIgnoreCase(pgStatus)) {
                paymentCommandService.processPaidWebhook(impUid);
                log.info("웹훅 결제 완료 처리 성공: imp_uid={}", impUid);
            } else if ("FAILED".equalsIgnoreCase(pgStatus) || "CANCELLED".equalsIgnoreCase(pgStatus)) {
                paymentCommandService.processFailedWebhook(impUid, "PG 웹훅 실패/취소 수신");
                log.info("웹훅 결제 실패/취소 처리 성공: imp_uid={}", impUid);
            } else {
                log.warn("처리되지 않는 PG 상태 웹훅 수신: imp_uid={}, pgStatus={}", impUid, pgStatus);
            }
        } catch (Exception e) {
            log.error("웹훅 처리 중 에러 발생: imp_uid={}", impUid, e);
            throw e;
        }
    }
}
