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
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 将 secret properties 解密
 * <p>
 *     cipher.length(固定两位数) + cipher + encrypt
 * </p>
 * @author maheng
 */
@Log4j2
public class SecretPropertySourceResolver implements ApplicationContextInitializer<ConfigurableApplicationContext>, ApplicationListener<ContextRefreshedEvent> {

    public static final String SECRET_PROPERTY_SOURCE_NAME = PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME+"-secretPropertySource";


    private final Decryptor decryptor;

    public SecretPropertySourceResolver() {
        try {
            List<Decryptor> decrypters = SpringFactoriesLoader.loadFactories(Decryptor.class, getClass().getClassLoader());
            this.decryptor = new CompositeDecryptor(decrypters);
        }catch (Exception e){
            log.error("Could not initialize SecretPropertySourceResolver, load Decryptor error.", e);
            throw e;
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        decrypt(applicationContext);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() instanceof ConfigurableApplicationContext applicationContext) {
            decrypt(applicationContext);
        }
    }

    private void decrypt (ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        //支持 多个secretPropertySource: bootstrapProperties-secretPropertySource-application-profile
        for (PropertySource<?> propertySource : mutablePropertySources) {
            //这里必须跟 BootstrapPropertySource 比较，解密完替换为 MapPropertiesSource
            //如果直接跟 MapPropertiesSource 比较，会重复解密，导致报错
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

    private String decryptValue (Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String string = value.toString();

        //如果原始字符串为空，
        if (string.length() <= 2) {
            return string;
        }
        int keyLength = Integer.parseInt(string.substring(0, 2));
        int index = keyLength + 2;
        String key = string.substring(2, index);
        String decrypted = string.substring(index);
        return decrypt(decrypted, key);
    }

    private String decrypt(String value, String key) {
        try {
            return decryptor.decrypt(value, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
