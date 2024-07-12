package io.github.opensabe.paypal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabe.common.core.AppException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.SubscriptionsApi;
import org.openapitools.client.api.VerifyWebhookSignatureApi;
import org.openapitools.client.model.Event;
import org.openapitools.client.model.Subscription;
import org.openapitools.client.model.VerifyWebhookSignature;
import org.openapitools.client.model.VerifyWebhookSignatureResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

//todo 在 github action 里面加入 secret，之后通过环境变量读取
@Disabled
@Log4j2
@SpringBootTest(classes = App.class)
public class SubscriptionsApiTest {
    @Autowired
    private PayPalService payPalService;

    @Test
    public void subscriptionGetTest() throws AppException, ApiException {
        ApiClient apiClient = new ApiClient();
        String token = payPalService.getToken();
        apiClient.setAccessToken(token);
        apiClient.setServerIndex(0);
        Subscription subscription = new SubscriptionsApi(apiClient).subscriptionsGet("I-HVS59UDE6C22", null);
        log.info(subscription);
    }

    @Test
    public void verifyTest() throws AppException, ApiException, JsonProcessingException {
        ApiClient apiClient = new ApiClient();
        ObjectMapper objectMapper = apiClient.getObjectMapper();
        String token = payPalService.getToken();
        apiClient.setAccessToken(token);
        apiClient.setServerIndex(0);
        String originBody = "{\"id\":\"WH-xxx-xxx\",\"event_version\":\"1.0\",\"create_time\":\"2024-06-04T06:39:06.310Z\",\"resource_type\":\"sale\",\"event_type\":\"PAYMENT.SALE.COMPLETED\",\"summary\":\"Payment completed for $ 6.6 USD\",\"resource\":{\"amount\":{\"total\":\"6.60\",\"currency\":\"USD\",\"details\":{\"subtotal\":\"6.60\"}},\"payment_mode\":\"INSTANT_TRANSFER\",\"create_time\":\"2024-06-04T06:39:01Z\",\"custom\":\"asd\",\"transaction_fee\":{\"currency\":\"USD\",\"value\":\"0.52\"},\"billing_agreement_id\":\"I-xxxx\",\"update_time\":\"2024-06-04T06:39:01Z\",\"protection_eligibility_type\":\"ITEM_NOT_RECEIVED_ELIGIBLE,UNAUTHORIZED_PAYMENT_ELIGIBLE\",\"protection_eligibility\":\"ELIGIBLE\",\"links\":[{\"method\":\"GET\",\"rel\":\"self\",\"href\":\"https://api.sandbox.paypal.com/v1/payments/sale/xxxx\"},{\"method\":\"POST\",\"rel\":\"refund\",\"href\":\"https://api.sandbox.paypal.com/v1/payments/sale/xxxx/refund\"}],\"id\":\"561246001M613742K\",\"state\":\"completed\",\"invoice_number\":\"\"},\"links\":[{\"href\":\"https://api.sandbox.paypal.com/v1/notifications/webhooks-events/WH-xxxx\",\"rel\":\"self\",\"method\":\"GET\"},{\"href\":\"https://api.sandbox.paypal.com/v1/notifications/webhooks-events/WH-xxxx/resend\",\"rel\":\"resend\",\"method\":\"POST\"}]}";
        OffsetDateTime transmissionTime = OffsetDateTime.parse("2024-06-04T06:39:14Z", DateTimeFormatter.ISO_DATE_TIME);
        Event eventOrigin = objectMapper.readValue(originBody, Event.class);
        VerifyWebhookSignature verifyWebhookSignature = new VerifyWebhookSignature()
                .authAlgo("SHA256withRSA")
                .certUrl(URI.create("https://api.sandbox.paypal.com/v1/notifications/certs/CERT-xxxx"))
                .transmissionId("xxx")
                .transmissionSig("xxxx")
                .transmissionTime(transmissionTime)
                .webhookId("xxx")
                .webhookEvent(eventOrigin);

        VerifyWebhookSignatureResponse verifyWebhookSignatureResponse = new VerifyWebhookSignatureApi(apiClient).verifyWebhookSignaturePost(verifyWebhookSignature);
        log.info(verifyWebhookSignatureResponse);
        log.info(objectMapper.writeValueAsString(eventOrigin));
    }
}
