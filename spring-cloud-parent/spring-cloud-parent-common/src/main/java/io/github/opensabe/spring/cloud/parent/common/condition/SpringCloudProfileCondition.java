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
