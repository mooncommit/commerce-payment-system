package com.example.commercepaymentsystem.infra.portone.client;

import com.example.commercepaymentsystem.global.exception.BusinessException;
import com.example.commercepaymentsystem.global.exception.ErrorCode;
import com.example.commercepaymentsystem.infra.portone.config.PortOneProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PortOneClientTest {

    @Test
    void getPaymentThrowsPortOneApiErrorWhenPortOneRequestFails() {
        TestPortOneClient testClient = createTestClient();
        testClient.server.expect(requestTo("https://api.portone.test/payments/pay_test"))
                .andRespond(withServerError());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> testClient.client.getPayment("pay_test")
        );

        assertEquals(ErrorCode.PORTONE_API_ERROR, exception.getErrorCode());
        testClient.server.verify();
    }

    @Test
    void getPaymentThrowsPortOneApiErrorWhenPortOneResponseIsEmpty() {
        TestPortOneClient testClient = createTestClient();
        testClient.server.expect(requestTo("https://api.portone.test/payments/pay_test"))
                .andRespond(withSuccess());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> testClient.client.getPayment("pay_test")
        );

        assertEquals(ErrorCode.PORTONE_API_ERROR, exception.getErrorCode());
        testClient.server.verify();
    }

    private static TestPortOneClient createTestClient() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.portone.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PortOneClient client = new PortOneClient(builder.build(), new PortOneProperties());

        return new TestPortOneClient(client, server);
    }

    private record TestPortOneClient(
            PortOneClient client,
            MockRestServiceServer server
    ) {
    }
}
