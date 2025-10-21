package io.github.opensabe.common.secret;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = SecretAnnotationProperties.PREFIX)
public class SecretAnnotationProperties {

    public static final String PREFIX = "customer";


    private String userName;

    @SecretProperty
    private String password;
}
