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
package io.github.opensabe.common.cache.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "caches")
public class CachesProperties {

    private List<CustomCacheProperties> custom;
    private boolean enabled = true;

    @Getter
    @Setter
    public static class CustomCacheProperties extends CacheProperties{

        private String cacheDesc;


        public Object getCacheSetting(){
            return switch (getType()) {
                case CAFFEINE -> getCaffeine();
                case COUCHBASE -> getCouchbase();
//                case EHCACHE:
//                    return getEhcache();
                case INFINISPAN -> getInfinispan();
                case JCACHE -> getJcache();
                case REDIS -> getRedis();
                default -> null;
            };
        }
    }
}
