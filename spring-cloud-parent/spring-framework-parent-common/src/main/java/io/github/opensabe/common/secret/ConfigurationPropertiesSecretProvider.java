package io.github.opensabe.common.secret;

import com.google.common.collect.Sets;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * 处理ConfigurationProperties中的敏感属性，在属性上添加@SecretProperty注解,即可
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
        Collection<Object> objects = beans.values();
        Map<String, Set<String>> result = new ConcurrentHashMap<>();
        for (Object object : objects) {
            ConfigurationProperties configurationProperties = AnnotatedElementUtils.findMergedAnnotation(object.getClass(), ConfigurationProperties.class);
            boolean classSecret = AnnotatedElementUtils.hasAnnotation(object.getClass(), SecretProperty.class);
            ReflectionUtils.doWithFields(object.getClass(), field -> {
                boolean secret = classSecret || AnnotatedElementUtils.hasAnnotation(field, SecretProperty.class);
                if (secret) {
                    String prefix = configurationProperties.prefix();
                    String key = prefix + "." + field.getName();
                    field.setAccessible(true);
                    Object value = ReflectionUtils.getField(field, object);
                    if (Objects.nonNull(value)) {
                        if (result.containsKey(key)) {
                            result.get(key).add(String.valueOf(value));
                        } else {
                            result.put(key, Sets.newHashSet(String.valueOf(value)));
                        }
                    }
                }

            });
        }
        return result;
    }
}
