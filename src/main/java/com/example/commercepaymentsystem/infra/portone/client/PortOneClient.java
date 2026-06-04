package com.example.commercepaymentsystem.infra.portone.client;

import com.example.commercepaymentsystem.domain.payment.port.PaymentGateway;
import com.example.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;
import com.example.commercepaymentsystem.infra.portone.config.PortOneProperties;
import com.example.commercepaymentsystem.infra.portone.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneClient implements PaymentGateway {

    private final RestClient portOneRestClient;
    private final PortOneProperties portOneProperties;

    @Override
    public PaymentGatewayResponse getPayment(String paymentId) {
        PortOnePaymentResponse response = portOneRestClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .retrieve()
                .body(PortOnePaymentResponse.class);

        if (response == null) {
            throw new IllegalStateException("PortOne 결제 조회 응답이 비어 있습니다.");
        }

        String transactionId = response.transactionId() != null ? response.transactionId() : response.id();
        long totalAmount = response.amount() != null ? response.amount().total() : 0L;

        return new PaymentGatewayResponse(transactionId, response.status(), totalAmount);
    }

    @Override
    public void cancelPayment(String paymentId, String reason) {
        portOneRestClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .body(new PortOneCancelRequest(portOneProperties.getStoreId(), reason))
                .retrieve()
                .toBodilessEntity();
    }

    private record PortOneCancelRequest(String storeId, String reason) {
    }
}
