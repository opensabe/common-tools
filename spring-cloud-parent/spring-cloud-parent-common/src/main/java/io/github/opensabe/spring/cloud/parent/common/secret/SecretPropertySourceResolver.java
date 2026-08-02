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
package io.github.opensabe.spring.cloud.parent.common.secret;

import io.github.opensabe.common.secret.Decryptor;
import io.github.opensabe.common.utils.AesGcm128Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Config Server Secret 属性源解密器。
 * <p>
 * 配置值格式：{@code base64(AES_GCM_128([4字节密钥长度] + [AES密钥] + [MySQL AES 密文]))}。
 * 解密后将 {@link org.springframework.cloud.bootstrap.config.BootstrapPropertySource}
 * 替换为 {@link org.springframework.core.env.MapPropertySource}，避免重复解密。
 *
 * @author maheng
 */
@Log4j2
public class SecretPropertySourceResolver implements ApplicationContextInitializer<ConfigurableApplicationContext>, ApplicationListener<ApplicationEvent>, InitializingBean {

    /** Bootstrap Secret 属性源名称前缀。 */
    public static final String SECRET_PROPERTY_SOURCE_NAME = PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME+"-secretPropertySource";



    /** 复合解密器。 */
    private CompositeDecryptor decryptor;

    /** Spring 注入的可选 SPI 解密器列表。 */
    @Autowired(required = false)
    private List<Decryptor> decrypters;

//
//    public SecretPropertySourceResolver() {
//        try {
//            List<Decryptor> decrypters = SpringFactoriesLoader.loadFactories(Decryptor.class, getClass().getClassLoader());
//            this.decryptor = new CompositeDecryptor(decrypters);
//        }catch (Exception e){
//            log.error("Could not initialize SecretPropertySourceResolver, load Decryptor error.", e);
//            throw e;
//        }
//    }

    /** ApplicationContext 初始化阶段解密 Secret 属性源。 */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        decrypt(applicationContext);
    }

    /** Context 刷新或 Environment 变更时重新解密。 */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent contextRefreshedEvent) {
            if (contextRefreshedEvent.getApplicationContext() instanceof ConfigurableApplicationContext applicationContext) {
                decrypt(applicationContext);
            }
        }else if (event instanceof EnvironmentChangeEvent environmentChangeEvent) {
            if (environmentChangeEvent.getSource() instanceof ConfigurableApplicationContext context) {
                decrypt(context);
            }
        }
    }

    private void decrypt (ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        //支持 多个secretPropertySource: bootstrapProperties-secretPropertySource-application-profile
        for (PropertySource<?> propertySource : mutablePropertySources) {
            //这里必须跟 BootstrapPropertySource 比较，解密完替换为 MapPropertiesSource
            //如果直接跟 MapPropertiesSource 比较，会重复解密，导致报错
            if (propertySource.getName().startsWith(SECRET_PROPERTY_SOURCE_NAME)) {
                if (propertySource instanceof BootstrapPropertySource<?> bootstrapPropertySource) {
                    String[] names = bootstrapPropertySource.getPropertyNames();
                    Map<String, Object> map = new HashMap<>(names.length);
                    for (String name : names) {
                        Object value = bootstrapPropertySource.getProperty(name);
                        try {
                            value = decryptValue(value);
                        }catch (Exception e) {
                            log.warn("SecretPropertySourceResolver.decrypt Unable to decrypt property,key: {}, message: {}", name, e.getMessage());
                        }
                        if (Objects.nonNull(value)) {
                            map.put(name, value);
                        }
                    }
                    mutablePropertySources.replace(propertySource.getName(), new MapPropertySource(propertySource.getName(), map));
                }
            }
        }


    }

    private String decryptValue (Object value) throws Exception {
        if (Objects.isNull(value)) {
            return null;
        }
        String string = value.toString();

        //[4字节 密钥长度] + [AES密钥] + [MySQL中的AES密文]
        byte[] bytes = AesGcm128Util.decryptBase64(string);

        if (bytes.length < Integer.BYTES) {
            throw new IllegalStateException("Invalid secret payload");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int keyLength = buffer.getInt();

        if (keyLength <0 || keyLength > buffer.remaining()) {
            throw new IllegalStateException("Invalid AES key length: " + keyLength);
        }

        byte[] key = new byte[keyLength];
        buffer.get(key);

        byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);

        return decrypt(new String(payload, StandardCharsets.UTF_8), new String(key, StandardCharsets.UTF_8));
    }

    private String decrypt(String value, String key) {
        try {
            return decryptor.decrypt(value, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 装配 CompositeDecryptor。 */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.decryptor = new CompositeDecryptor(decrypters);
    }
}
