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

import com.google.common.collect.Sets;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * 处理ConfigurationProperties中的敏感属性，在属性上添加@SecretProperty注解,即可
 * TODO 支持对象属性
 * @author hengma
 */
public class ConfigurationPropertiesSecretProvider extends SecretProvider implements ApplicationContextAware {


    @Setter
    private ApplicationContext applicationContext;
    public ConfigurationPropertiesSecretProvider(GlobalSecretManager globalSecretManager) {
        super(globalSecretManager);
    }

    @Override
    protected String name() {
        return "ConfigurationPropertiesSecret";
    }

    @Override
    protected long reloadTimeInterval() {
        return 60;
    }

    @Override
    protected TimeUnit reloadTimeIntervalUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    protected Map<String, Set<String>> reload() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ConfigurationProperties.class);
        Map<String, Set<String>> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {

            Object object = entry.getValue();
            String beanName = entry.getKey();

            ConfigurationProperties configurationProperties = object.getClass().getAnnotation(ConfigurationProperties.class);

            // 如果类上没有，则尝试从Bean定义中获取
            String prefix = "";
            if (configurationProperties != null) {
                prefix = configurationProperties.prefix();
            } else if (applicationContext instanceof ConfigurableApplicationContext configurableApplicationContext) {
                // 尝试从Bean工厂获取方法级别的注解
                try {
                    ConfigurableListableBeanFactory beanFactory = configurableApplicationContext.getBeanFactory();
                    if (beanFactory instanceof DefaultListableBeanFactory defaultListableBeanFactory) {
                        BeanDefinition beanDefinition = defaultListableBeanFactory.getBeanDefinition(beanName);
                        if (beanDefinition instanceof RootBeanDefinition rootBeanDefinition) {
                            Method factoryMethod = rootBeanDefinition.getResolvedFactoryMethod();
                            if (factoryMethod != null) {
                                ConfigurationProperties methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(factoryMethod, ConfigurationProperties.class);
                                if (methodAnnotation != null) {
                                    prefix = methodAnnotation.prefix();
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // 忽略异常，使用默认前缀
                }
            }



            boolean classSecret = AnnotatedElementUtils.hasAnnotation(object.getClass(), SecretProperty.class);
            // 递归处理对象
            processObject(prefix, object, classSecret, result);


        }
        return result;
    }

    /**
     * 递归处理对象及其嵌套属性
     */
    private void processObject(String prefix, Object object, boolean parentSecret, Map<String, Set<String>> result) {
        if (object == null) {
            return;
        }

        Class<?> clazz = object.getClass();

        // 如果当前字段标记为敏感，添加到结果中
        if (parentSecret && object instanceof String string) {
            if (result.containsKey(prefix)) {
                result.get(prefix).add(string);
            } else {
                result.put(prefix, Sets.newHashSet(string));
            }
            return;
        }
        if (isSimpleType(clazz)) {
            return;
        }

        if (clazz.isRecord()) {
            for (RecordComponent component : clazz.getRecordComponents()) {
                try {
                    Object value = component.getAccessor().invoke(object);
                    processObject(prefix + "." + component.getName(), value, parentSecret || component.isAnnotationPresent(SecretProperty.class), result);
                } catch (IllegalAccessException | InvocationTargetException ignore) {
                }
            }
        }

        ReflectionUtils.doWithFields(clazz, field -> {
            if (Modifier.isStatic(field.getModifiers())) {
                return;
            }
            if (!field.trySetAccessible()) {
                return;
            }
            boolean secret = parentSecret || AnnotatedElementUtils.hasAnnotation(field, SecretProperty.class);
            Object value = ReflectionUtils.getField(field, object);
            
            if (Objects.isNull(value)) {
                return;
            }
            
            String key = prefix + "." + field.getName();


            //处理map
            if (value instanceof Map<?, ?> map) {
                map.forEach((k, v) ->  processObject(key + "." + k, v, secret, result));
            }
            //处理数组,我们忽略Primitive类型，因为我们不可能去把一个数字进行脱敏
            if (value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
                Object[] array = (Object[]) value;
                for (Object o : array) {
                    processObject(key, o, secret, result);
                }
            }

            //处理集合
            if (value instanceof Collection<?> collection) {
                collection.forEach(v ->  processObject(key, v, secret, result));
            }

            processObject(key, value, secret, result);
        });
    }
    

    


    
    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> type) {
        Package packageName;
        if ((packageName = type.getPackage()) == null) {
            return false;
        }
        return type.isPrimitive() ||
               type == Boolean.class ||
               type == Character.class ||
               type == Byte.class ||
               type == Short.class ||
               type == Integer.class ||
               type == Long.class ||
               type == Float.class ||
               type == Double.class ||
               type == Void.class ||
               type.isEnum() ||
               packageName.getName().startsWith("java.") ||
               packageName.getName().startsWith("javax.") ||
               packageName.getName().startsWith("org.springframework.");
    }
}