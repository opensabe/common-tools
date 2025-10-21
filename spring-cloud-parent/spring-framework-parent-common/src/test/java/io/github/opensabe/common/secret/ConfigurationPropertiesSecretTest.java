package io.github.opensabe.common.secret;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableConfigurationProperties({SecretAnnotationProperties.class})
@DisplayName("测试@ConfigurationProperties脱敏")
@SpringBootTest(properties = {
        "customer.user-name=testUser",
        "customer.password=testPassword123"
},classes = ConfigurationPropertiesSecretTest.App.class)
public class ConfigurationPropertiesSecretTest {

    @SpringBootApplication(scanBasePackages = "io.github.opensabe.common.auto")
    public static class App {

    }

    @Autowired
    private SecretAnnotationProperties secretAnnotationProperties;

    @Autowired
    private GlobalSecretManager globalSecretManager;


    @DisplayName("通过@SecretProperty注解脱敏")
    @Test
    void testAnnotation () {
        FilterSecretStringResult result = globalSecretManager.filterSecretStringAndAlarm("jdbc.password is "+secretAnnotationProperties.getPassword());
        assertTrue(result.isFoundSensitiveString());
        assertEquals("jdbc.password is ******", result.getFilteredContent());
    }
}
