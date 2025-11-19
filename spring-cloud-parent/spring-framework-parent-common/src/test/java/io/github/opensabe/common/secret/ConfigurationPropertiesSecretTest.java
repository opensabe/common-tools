/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
