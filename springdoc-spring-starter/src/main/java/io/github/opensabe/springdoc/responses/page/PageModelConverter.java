package io.github.opensabe.springdoc.responses.page;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.swagger.v3.core.util.Json;

import java.util.ArrayList;
import java.util.List;


/**
 * spring data relation中Page简化字段，只返回 list 和 total
 * @author heng.ma
 */
public class PageModelConverter {

    public static void config () {
        ObjectMapper mapper = Json.mapper();
        SerializationConfig config = mapper.getSerializationConfig();
        config = config.with(new ClassIntrospectorDecorator(config.getClassIntrospector()) {
            @Override
            public BeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) {
                BeanDescription delegate = super.forSerialization(cfg, type, r);
                if ("org.springframework.data.domain.Page".equals(type.getRawClass().getName())) {
                    return new BeanDescriptionDecorator(delegate) {
                        @Override
                        public List<BeanPropertyDefinition> findProperties() {
                            List<BeanPropertyDefinition> properties = super.findProperties();
                            List<BeanPropertyDefinition> result = new ArrayList<>(2);
                            for (BeanPropertyDefinition property : properties) {
                                if ("content".equals(property.getName())) {
                                    result.add(property.withSimpleName("list"));
                                }
                                if ("totalElements".equals(property.getName())) {
                                    result.add(property.withSimpleName("total"));
                                }
                            }
                            return result;
                        }
                    };
                }
                return delegate;
            }
        });
        mapper.setConfig(config);

    }
}
