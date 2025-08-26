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
package io.github.opensabe.common.location.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * @author changhongwei
 * @date 2025/1/21 17:20
 * @description:
 */
public enum LocationDocumentation implements ObservationDocumentation {
    LOCATION {
        @Override
        public String getName() {
            return "location.geocode";
        }

        @Override
        public Class<? extends LocationConvention> getDefaultConvention() {
            return LocationConvention.class;
        }
    };

    public enum LocationTag implements KeyName {
        METHOD_NAME {
            @Override
            public String asString() {
                return "location.method.name";
            }
        },
        REQUEST_PARAMS {
            @Override
            public String asString() {
                return "location.request.params";
            }
        },
        RESPONSE {
            @Override
            public String asString() {
                return "location.response";
            }
        },
        EXECUTION_TIME {
            @Override
            public String asString() {
                return "location.execution.time";
            }
        },
        LOCATION_SUCCESSFULLY {
            @Override
            public String asString() {
                return "location.successfully";
            }
        },
        THROWABLE {
            @Override
            public String asString() {
                return "location.throwable";
            }
        };


    }
}
