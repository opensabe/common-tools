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

import java.util.Set;


@DisplayName("测试从解密config server properties")
@SpringBootTest(classes = SecretPropertySourceTest.App.class,properties = {
        "eureka.client.enabled=false"
})
@EnableConfigurationProperties(SecretPropertySourceTest.FooProperties.class)
public class SecretPropertySourceTest {

    @SpringBootApplication
    public static class App {

    }



    @Autowired
    private MockConfigServerPropertySourceLocator propertySourceLocator;

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "foo")
    public static class FooProperties {

        private String bar;

        private String par;
    }


    @BeforeAll
    static void setup () {
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

    @Test
    @DisplayName("测试Environment中的属性是解密后的")
    void testEnvironment () {
        Assertions.assertEquals("foobar", environment.getProperty("foo.bar"));
        Assertions.assertEquals("foopar", environment.getProperty("foo.par"));
    }

    @Test
    @DisplayName("测试@ConfigurationProperties中的属性是解密后的")
    void testProperties () {
        Assertions.assertEquals("foobar", fooProperties.getBar());
        Assertions.assertEquals("foopar", fooProperties.getPar());
    }

    @Test
    @DisplayName("测试/actuator/env/refresh以后能更新")
    void testRefresh () {
        Assertions.assertEquals("foobar", environment.getProperty("ping.pong"));
        MockConfigServerPropertySourceLocator.put("ping.pong", "TJoYhg9kjpzWIG/HXMugMQ==", "tNYjSk4o1A3aeKAV2NZliO36AsV84VNcak5jAW6l+bs=");
        applicationContext.getEnvironment().getPropertySources().replace(SecretPropertySourceResolver.SECRET_PROPERTY_SOURCE_NAME, new BootstrapPropertySource<>((EnumerablePropertySource) propertySourceLocator.locate(null)));
        applicationContext.publishEvent(new EnvironmentChangeEvent(applicationContext, Set.of("ping.pong")));
        Assertions.assertEquals("foopar", environment.getProperty("ping.pong"));
    }


    @Test
    @DisplayName("测试spring.factories中定义的Decryptor能正常执行")
    void testCustomDecryptor () {
        org.assertj.core.api.Assertions.assertThat(CustomerDecrptor.getRun())
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("测试敏感信息可以脱敏")
    void testSensitivity() {
       String value  = "this is a log " + fooProperties.getBar();
        FilterSecretStringResult result = globalSecretManager.filterSecretStringAndAlarm(value);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isFoundSensitiveString());
        String content = result.getFilteredContent().replace("this is a log ", "");
        Assertions.assertEquals(GlobalSecretManager.MASKER, content);
    }
}
