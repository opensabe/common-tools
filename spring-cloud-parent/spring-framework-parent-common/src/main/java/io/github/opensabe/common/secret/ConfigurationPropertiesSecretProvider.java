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
import java.util.Arrays;


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
            String prefix = configurationProperties.prefix();
            
            // 使用递归处理器处理对象
            processObjectWithAnnotations(prefix, object, classSecret, result);
        }
        
        return result;
    }
    
    /**
     * 处理对象及其嵌套属性，参考Spring Validation的设计方式
     */
    private void processObjectWithAnnotations(String prefix, Object object, boolean parentSecret, Map<String, Set<String>> result) {
        if (object == null) {
            return;
        }
        
        // 对于基本类型或简单类型，直接返回
        if (isSimpleType(object.getClass())) {
            return;
        }
        
        // 处理Map类型
        if (object instanceof Map) {
            processMapWithAnnotations(prefix, (Map<?, ?>) object, parentSecret, result);
            return;
        }
        
        // 处理Collection类型
        if (object instanceof Collection) {
            processCollectionWithAnnotations(prefix, (Collection<?>) object, parentSecret, result);
            return;
        }
        
        // 处理数组类型
        if (object.getClass().isArray()) {
            processArrayWithAnnotations(prefix, object, parentSecret, result);
            return;
        }
        
        // 处理普通对象（包括自定义类）
        ReflectionUtils.doWithFields(object.getClass(), field -> {
            boolean secret = parentSecret || AnnotatedElementUtils.hasAnnotation(field, SecretProperty.class);
            field.setAccessible(true);
            Object value = ReflectionUtils.getField(field, object);
            
            if (Objects.isNull(value)) {
                return;
            }
            
            String key = prefix + "." + field.getName();
            
            // 如果当前字段标记为敏感，添加到结果中
            if (secret) {
                addSecretValue(key, value, result);
            }
            
            // 递归处理嵌套属性，注意传递secret状态
            processObjectWithAnnotations(key, value, secret, result);
        });
    }
    
    /**
     * 处理Map类型的注解
     */
    private void processMapWithAnnotations(String prefix, Map<?, ?> map, boolean parentSecret, Map<String, Set<String>> result) {
        map.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            
            String itemPrefix = prefix + "." + key;
            
            // 如果父级标记为敏感，添加当前Map值
            if (parentSecret) {
                addSecretValue(itemPrefix, value, result);
            }
            
            // 递归处理Map中的值
            processObjectWithAnnotations(itemPrefix, value, parentSecret, result);
        });
    }
    
    /**
     * 处理Collection类型的注解
     */
    private void processCollectionWithAnnotations(String prefix, Collection<?> collection, boolean parentSecret, Map<String, Set<String>> result) {
        int index = 0;
        for (Object item : collection) {
            if (item == null) {
                continue;
            }
            
            String itemPrefix = prefix + "[" + index + "]";
            
            // 如果父级标记为敏感，添加当前集合元素
            if (parentSecret) {
                addSecretValue(itemPrefix, item, result);
            }
            
            // 递归处理集合中的元素
            processObjectWithAnnotations(itemPrefix, item, parentSecret, result);
            index++;
        }
    }
    
    /**
     * 处理数组类型的注解
     */
    private void processArrayWithAnnotations(String prefix, Object array, boolean parentSecret, Map<String, Set<String>> result) {
        Object[] objects = array instanceof Object[] ? (Object[]) array : convertPrimitiveArrayToObjectArray(array);
        if (objects == null) {
            return;
        }
        
        for (int i = 0; i < objects.length; i++) {
            Object item = objects[i];
            if (item == null) {
                continue;
            }
            
            String itemPrefix = prefix + "[" + i + "]";
            
            // 如果父级标记为敏感，添加当前数组元素
            if (parentSecret) {
                addSecretValue(itemPrefix, item, result);
            }
            
            // 递归处理数组中的元素
            processObjectWithAnnotations(itemPrefix, item, parentSecret, result);
        }
    }
    
    /**
     * 添加敏感值到结果集合中
     */
    private void addSecretValue(String key, Object value, Map<String, Set<String>> result) {
        if (value != null) {
            result.computeIfAbsent(key, k -> Sets.newHashSet()).add(String.valueOf(value));
        }
    }
    
    /**
     * 将基本类型数组转换为对象数组
     */
    private Object[] convertPrimitiveArrayToObjectArray(Object array) {
        if (array instanceof int[]) {
            return Arrays.stream((int[]) array).boxed().toArray(Integer[]::new);
        } else if (array instanceof long[]) {
            return Arrays.stream((long[]) array).boxed().toArray(Long[]::new);
        } else if (array instanceof double[]) {
            return Arrays.stream((double[]) array).boxed().toArray(Double[]::new);
        } else if (array instanceof float[]) {
            return Arrays.stream((float[]) array).boxed().toArray(Float[]::new);
        } else if (array instanceof boolean[]) {
            return Arrays.stream((boolean[]) array).boxed().toArray(Boolean[]::new);
        } else if (array instanceof char[]) {
            return Arrays.stream((char[]) array).boxed().toArray(Character[]::new);
        } else if (array instanceof byte[]) {
            return Arrays.stream((byte[]) array).boxed().toArray(Byte[]::new);
        } else if (array instanceof short[]) {
            return Arrays.stream((short[]) array).boxed().toArray(Short[]::new);
        }
        return null;
    }
    
    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || 
               type == String.class ||
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
               java.util.Date.class.isAssignableFrom(type) ||
               java.time.temporal.Temporal.class.isAssignableFrom(type);
    }
}