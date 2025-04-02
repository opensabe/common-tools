package io.github.opensabe.apple;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.client.BearerTokenAuthenticator;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.*;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabe.apple.appstoreconnectapi.AppleStoreConnectAPIClient;
import io.github.opensabe.apple.appstoreconnectapi.inapppurchasesv2.InAppPurchasesV2Response;
import io.github.opensabe.apple.appstoreconnectapi.subscriptiongroup.SubscriptionGroupsResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

//todo 在 github action 里面加入 secret，之后通过环境变量读取
@Disabled
public class AppleAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("apple.in-purchase.enable=true",
                    "apple.in-purchase.signing-key=${apple.in-purchase.signing-key}",
                    "apple.in-purchase.key-id=${apple.in-purchase.key-id}",
                    "apple.in-purchase.issuer-id=${apple.in-purchase.issuer-id}",
                    "apple.in-purchase.bundle-id=${apple.in-purchase.bundle-id}",
                    "apple.in-purchase.environment=SANDBOX",
                    "apple.in-purchase.app-apple-id=${apple.in-purchase.app-apple-id}",
                    "apple.in-purchase.enable-online-checks=true",

                    "apple.store.connect.enable=true",
                    "apple.store.connect.signing-key=${apple.store.connect.signing-key}",
                    "apple.store.connect.key-id=${apple.store.connect.key-id}",
                    "apple.store.connect.issuer-id=${apple.store.connect.issuer-id}",
                    "apple.store.connect.bundle-id=${apple.store.connect.bundle-id}",
                    "apple.store.connect.app-apple-id=${apple.store.connect.app-apple-id}",

                    "apple.login.enable=true",
                    "apple.login.web.signing-key=${apple.login.web.signing-key}",
                    "apple.login.web.key-id=${apple.login.web.key-id}",
                    "apple.login.web.issuer-id=${apple.login.web.issuer-id}",
                    "apple.login.web.bundle-id=${apple.login.web.bundle-id}",
                    "apple.login.web.redirect-uri=${apple.login.web.redirect-uri}",

                    "apple.login.ios.signing-key=${apple.login.ios.signing-key}",
                    "apple.login.ios.key-id=${apple.login.ios.key-id}",
                    "apple.login.ios.issuer-id=${apple.login.ios.issuer-id}",
                    "apple.login.ios.bundle-id=${apple.login.ios.bundle-id}"
            )
            .withConfiguration(AutoConfigurations.of(AppleAutoConfiguration.class));

    public static final String RECEIPT_SIGN = "${RECEIPT_SIGN}";

    @Test
    public void appleAutoConfigurationTest() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(AppleInPurchaseProperties.class);
                    AppleInPurchaseProperties appleInPurchaseProperties = context.getBean(AppleInPurchaseProperties.class);
                    Assertions.assertEquals("${apple.in-purchase.signing-key}", appleInPurchaseProperties.getSigningKey());
                    Assertions.assertEquals("${apple.in-purchase.key-id}", appleInPurchaseProperties.getKeyId());
                    Assertions.assertEquals("${apple.in-purchase.issuer-id}", appleInPurchaseProperties.getIssuerId());
                    Assertions.assertEquals("${apple.in-purchase.bundle-id}", appleInPurchaseProperties.getBundleId());
                    Assertions.assertEquals(Environment.SANDBOX, appleInPurchaseProperties.getEnvironment());
                    Assertions.assertEquals(0L, appleInPurchaseProperties.getAppAppleId());
                    assertThat(context).hasSingleBean(AppStoreServerAPIClient.class);
                    assertThat(context).hasSingleBean(SignedDataVerifier.class);
                    assertThat(context).hasSingleBean(ReceiptUtility.class);
                });
    }

    @Test
    public void appleInPurchaseReceiptDecodeTest() {
        contextRunner
                .run(context -> {
                    ReceiptUtility receiptUtility = context.getBean(ReceiptUtility.class);
                    String transactionId = receiptUtility.extractTransactionIdFromAppReceipt(RECEIPT_SIGN);
                    AppStoreServerAPIClient appStoreServerAPIClient = context.getBean(AppStoreServerAPIClient.class);

                    TransactionInfoResponse transactionInfo = appStoreServerAPIClient.getTransactionInfo(transactionId);
                    SignedDataVerifier signedDataVerifier = context.getBean(SignedDataVerifier.class);
                    JWSTransactionDecodedPayload jwsTransactionDecodedPayload = signedDataVerifier.verifyAndDecodeTransaction(transactionInfo.getSignedTransactionInfo());
                    System.out.println(jwsTransactionDecodedPayload);
                });
    }

    public static final String NOTIFY_SIGNED_PAYLOAD_SUBSCRIBED_INITIAL_BUY = "${NOTIFY_SIGNED_PAYLOAD_SUBSCRIBED_INITIAL_BUY}";

    @Test
    public void appleInPurchaseNotifyDecodeSubscribedInitialBuyTest() {
        contextRunner
                .run(context -> {
                    SignedDataVerifier signedDataVerifier = context.getBean(SignedDataVerifier.class);
                    ResponseBodyV2DecodedPayload responseBodyV2DecodedPayload = signedDataVerifier.verifyAndDecodeNotification(NOTIFY_SIGNED_PAYLOAD_SUBSCRIBED_INITIAL_BUY);
                    System.out.println(responseBodyV2DecodedPayload);
                    NotificationTypeV2 notificationType = responseBodyV2DecodedPayload.getNotificationType();
                    Subtype subtype = responseBodyV2DecodedPayload.getSubtype();
                    String signedTransactionInfo = responseBodyV2DecodedPayload.getData().getSignedTransactionInfo();
                    JWSTransactionDecodedPayload jwsTransactionDecodedPayload = signedDataVerifier.verifyAndDecodeTransaction(signedTransactionInfo);
                    System.out.println(jwsTransactionDecodedPayload);
                    String signedRenewalInfo = responseBodyV2DecodedPayload.getData().getSignedRenewalInfo();
                    JWSRenewalInfoDecodedPayload jwsRenewalInfoDecodedPayload = signedDataVerifier.verifyAndDecodeRenewalInfo(signedRenewalInfo);
                    System.out.println(jwsRenewalInfoDecodedPayload);
                });
    }

    public static final String NOTIFY_SIGNED_PAYLOAD_DID_RENEW = "${NOTIFY_SIGNED_PAYLOAD_DID_RENEW}";

    @Test
    public void appleInPurchaseNotifyDecodeDidRenewTest() {
        contextRunner
                .run(context -> {
                    SignedDataVerifier signedDataVerifier = context.getBean(SignedDataVerifier.class);
                    ResponseBodyV2DecodedPayload responseBodyV2DecodedPayload = signedDataVerifier.verifyAndDecodeNotification(NOTIFY_SIGNED_PAYLOAD_DID_RENEW);
                    System.out.println(responseBodyV2DecodedPayload);
                    String signedTransactionInfo = responseBodyV2DecodedPayload.getData().getSignedTransactionInfo();
                    JWSTransactionDecodedPayload jwsTransactionDecodedPayload = signedDataVerifier.verifyAndDecodeTransaction(signedTransactionInfo);
                    System.out.println(jwsTransactionDecodedPayload);
                });
    }

    public static final String NOTIFY_SIGNED_PAYLOAD_EXPIRED_VOLUNTARY = "${NOTIFY_SIGNED_PAYLOAD_EXPIRED_VOLUNTARY}";

    @Test
    public void appleInPurchaseNotifyDecodeExpiredVoluntaryTest() {
        contextRunner
                .run(context -> {
                    SignedDataVerifier signedDataVerifier = context.getBean(SignedDataVerifier.class);
                    ResponseBodyV2DecodedPayload responseBodyV2DecodedPayload = signedDataVerifier.verifyAndDecodeNotification(NOTIFY_SIGNED_PAYLOAD_EXPIRED_VOLUNTARY);
                    System.out.println(responseBodyV2DecodedPayload);
                });
    }

    @Test
    public void appleAutoConfigurationNoAutoConfigurationTest() {
        contextRunner.withPropertyValues("apple.in-purchase.enable=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ReceiptUtility.class);
                });
    }

    @Test
    public void rootCertificateTest() {
        Set<InputStream> rootCertificates = AppleInPurchaseConfiguration.getRootCertificates();
        Assertions.assertEquals(rootCertificates.isEmpty(), Boolean.FALSE);
    }


    @Test
    public void appleStoreConnectApiClientTest() {
            ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .run(context -> {
                    assertThat(context).hasSingleBean(AppleStoreConnectAPIClient.class);
                    AppleStoreConnectAPIClient appleStoreConnectAPIClient = context.getBean(AppleStoreConnectAPIClient.class);
                    InAppPurchasesV2Response inAppPurchasesV2Response = appleStoreConnectAPIClient.inAppPurchasesV2();
                    System.out.println(inAppPurchasesV2Response.toString());

                    SubscriptionGroupsResponse subscriptionGroupsResponse = appleStoreConnectAPIClient.subscriptions("0");
                    System.out.println(subscriptionGroupsResponse.toString());
                });
    }


    @Test
    public void useInPurchaseSign() {
        String signingKey = "${signingKey}";
        String keyId = "${keyId}";
        String issuerId = "${issuerId}";
        String bundleId = "${bundleId}";
        BearerTokenAuthenticator bearerTokenAuthenticator = new BearerTokenAuthenticator(signingKey,
                keyId, issuerId, bundleId);
        String s = bearerTokenAuthenticator.generateToken();
        System.out.println(s);
    }

    @Test
    public void useAppleLoginSign() {

        String issuerId = "${issuerId}";
        String keyId = "${keyId}";
        String bundleId = "${bundleId}";
        String signingKey = "${signingKey}";
        AppleLoginClientSecretAuthenticator appleLoginClientSecretAuthenticator = new AppleLoginClientSecretAuthenticator(issuerId,
                keyId, bundleId,
                signingKey);

        System.out.println(appleLoginClientSecretAuthenticator.generateToken());
    }

    @Test
    public void appleLoginBeanTest() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AppleLoginProperties.class);
            assertThat(context).hasBean("appleWebLoginAPIClient");
            assertThat(context).hasBean("appleIosLoginAPIClient");

        });
    }

    @Test
    public void authTokenTest() {
        contextRunner.run(context -> {
            AppleLoginAPIClient appleWebLoginAPIClient = context.getBean("appleWebLoginAPIClient", AppleLoginAPIClient.class);
            String code = "";
            AppleLoginAPIClient.TokenResponse tokenResponse = appleWebLoginAPIClient.authToken(code);
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(tokenResponse));
        });
    }

    @Test
    public void authKeysTest() {
        contextRunner.run(context -> {
            AppleLoginAPIClient appleWebLoginAPIClient = context.getBean("appleWebLoginAPIClient", AppleLoginAPIClient.class);
            AppleLoginAPIClient.AuthKeys authKeys = appleWebLoginAPIClient.authKeys();
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(authKeys));
        });
    }

    @Test
    public void decodeIdTokenTest() {
        String idToken = "${idToken}";
        DecodedJWT decode = JWT.decode(idToken);
        String keyId = decode.getKeyId();
        String issuer = decode.getIssuer();
        List<String> audience = decode.getAudience();
        Date expiresAt = decode.getExpiresAt();
        String email = decode.getClaims().get("email").asString();
    }

    @Test
    public void verifyUserTest() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("appleWebLoginUtility");
            AppleLoginUtility appleWebLoginUtility = context.getBean("appleWebLoginUtility", AppleLoginUtility.class);
            String code = "";
            AppleLoginUtility.VerifyUserResult verifyUserResult = appleWebLoginUtility.verifyUser(code);
            System.out.println(verifyUserResult);
        });
    }
}
