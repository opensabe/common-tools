package io.github.opensabe.paypal.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypal.api")
@Data
public class PayPalProperties {

    /**
     * PayPal api的url地址
     */
    @NotBlank
    private String url;

    /**
     * PayPal 的 client_id
     */
    @NotBlank
    private String clientId;

    /**
     * PayPal 的 client secret
     */
    @NotBlank
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
