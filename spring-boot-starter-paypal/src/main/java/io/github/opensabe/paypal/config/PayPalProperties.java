package io.github.opensabe.paypal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypal.api")
@Data
public class PayPalProperties {

    /**
     * PayPal api的url地址
     */
    private String url;

    /**
     * PayPal 的 client_id
     */
    private String clientId;

    /**
     * PayPal 的 client secret
     */
    private String clientSecret;


    /**
     * PayPal 的 server index 0:sandbox 1:living
     */
    private Integer serverIndex;

    /**
     * PayPal 的 web hook
     */
    private String webhookId;
}
