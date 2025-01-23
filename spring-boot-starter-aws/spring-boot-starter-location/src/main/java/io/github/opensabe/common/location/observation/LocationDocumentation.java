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

    public enum LOCATION_TAG implements KeyName {
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
