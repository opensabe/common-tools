package io.github.opensabe.spring.cloud.parent.common.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

import java.util.List;

public class SpringCloudProfileCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(ConditionOnSpringCloudConfigProfile.class.getName());
        if (attrs != null) {
            var predicate = (ConditionOnSpringCloudConfigProfile.Predicate)attrs.getOrDefault("predicate", List.of(ConditionOnSpringCloudConfigProfile.Predicate.equals)).get(0);
            for (Object value : attrs.get("value")) {
                var e = context.getEnvironment();
                var profile = e.getProperty("spring.cloud.config.profile");
                if (Profiles.of((String[]) value).matches(s -> predicate.matches(profile,s))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
