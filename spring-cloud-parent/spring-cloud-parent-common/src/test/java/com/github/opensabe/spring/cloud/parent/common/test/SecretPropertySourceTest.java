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
package com.github.opensabe.spring.cloud.parent.common.test;


import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.utils.AesGcm128Util;
import io.github.opensabe.spring.cloud.parent.common.secret.SecretPropertySourceResolver;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import java.util.HexFormat;
import java.util.Set;


/**
 * Secret 属性源解密与脱敏集成测试。
 * <p>
 * 验证 Config Server 加密属性在 Environment、{@code @ConfigurationProperties}
 * 与 refresh 后的解密行为，以及自定义 Decryptor 与日志脱敏。
 */
@DisplayName("Secret 属性源解密与脱敏")
@SpringBootTest(classes = SecretPropertySourceTest.App.class,properties = {
        "eureka.client.enabled=false"
})
@EnableConfigurationProperties(SecretPropertySourceTest.FooProperties.class)
public class SecretPropertySourceTest {

    /** 最小 Spring Boot 测试应用。 */
    @SpringBootApplication
    public static class App {

    }


    @Autowired
    private MockConfigServerPropertySourceLocator propertySourceLocator;

    /** 绑定 foo.* 配置的测试属性类。 */
    @Getter
    @Setter
    @ConfigurationProperties(prefix = "foo")
    public static class FooProperties {

        /** 测试 bar 属性。 */
        private String bar;

        /** 测试 par 属性。 */
        private String par;
    }


    @BeforeAll
    static void setup () {

        AesGcm128Util.setPSK(HexFormat.of().parseHex("7f2d189a3e5c0b678a1d0f2e3c4b5a69"));

        //mysql select to_base64(aes_encrypt('foobar', 'foo'))
        MockConfigServerPropertySourceLocator.put("foo.bar", "foo","Dzll4Tp79x73q9e+rnQXhA==");
        // AESUtil.encrypt("foobar","TJoYhg9kjpzWIG/HXMugMQ==")
        MockConfigServerPropertySourceLocator.put("foo.par", "TJoYhg9kjpzWIG/HXMugMQ==","tNYjSk4o1A3aeKAV2NZliO36AsV84VNcak5jAW6l+bs=");

        MockConfigServerPropertySourceLocator.put("ping.pong", "foo", "Dzll4Tp79x73q9e+rnQXhA==");
    }

    @Autowired
    private FooProperties fooProperties;

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableApplicationContext  applicationContext;

    @Autowired
    private GlobalSecretManager globalSecretManager;

    @Autowired
    private CustomerDecrptor customerDecrptor;

    @Test
    @DisplayName("Environment 中属性已解密")
    void testEnvironment () {
        Assertions.assertEquals("foobar", environment.getProperty("foo.bar"));
        Assertions.assertEquals("foopar", environment.getProperty("foo.par"));
    }

    @Test
    @DisplayName("@ConfigurationProperties 中属性已解密")
    void testProperties () {
        Assertions.assertEquals("foobar", fooProperties.getBar());
        Assertions.assertEquals("foopar", fooProperties.getPar());
    }

    @Test
    @DisplayName("refresh 后属性可更新")
    void testRefresh () {
        Assertions.assertEquals("foobar", environment.getProperty("ping.pong"));
        MockConfigServerPropertySourceLocator.put("ping.pong", "TJoYhg9kjpzWIG/HXMugMQ==", "tNYjSk4o1A3aeKAV2NZliO36AsV84VNcak5jAW6l+bs=");
        applicationContext.getEnvironment().getPropertySources().replace(SecretPropertySourceResolver.SECRET_PROPERTY_SOURCE_NAME, new BootstrapPropertySource<>((EnumerablePropertySource) propertySourceLocator.locate(null)));
        applicationContext.publishEvent(new EnvironmentChangeEvent(applicationContext, Set.of("ping.pong")));
        Assertions.assertEquals("foopar", environment.getProperty("ping.pong"));
    }


    @Test
    @DisplayName("SPI Decryptor 可正常执行")
    void testCustomDecryptor () {
        org.assertj.core.api.Assertions.assertThat(customerDecrptor.getRun())
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("敏感信息可脱敏")
    void testSensitivity() {
       String value  = "this is a log " + fooProperties.getBar();
        FilterSecretStringResult result = globalSecretManager.filterSecretStringAndAlarm(value);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isFoundSensitiveString());
        String content = result.getFilteredContent().replace("this is a log ", "");
        Assertions.assertEquals(GlobalSecretManager.MASKER, content);
    }
}
