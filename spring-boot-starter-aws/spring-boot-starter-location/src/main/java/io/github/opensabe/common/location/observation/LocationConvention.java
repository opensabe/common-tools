package io.github.opensabe.common.location.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * @author changhongwei
 * @date 2025/1/21 17:08
 * @description:
 */

public class LocationConvention implements ObservationConvention<LocationContext> {

    // 懒汉单例实例
    public static final LocationConvention DEFAULT = new LocationConvention();

    // supportsContext 实现
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof LocationContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(LocationContext context) {
        return KeyValues.of(
                LocationDocumentation.LOCATION_TAG.METHOD_NAME.withValue(String.valueOf(context.getMethodName())),
                LocationDocumentation.LOCATION_TAG.LOCATION_SUCCESSFULLY.withValue(String.valueOf(context.isSuccessful()))
         );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(LocationContext context) {
        return KeyValues.of(
                LocationDocumentation.LOCATION_TAG.REQUEST_PARAMS.withValue(String.valueOf(context.getRequestParams())),
                LocationDocumentation.LOCATION_TAG.RESPONSE.withValue(String.valueOf(context.getResponse())),
                LocationDocumentation.LOCATION_TAG.EXECUTION_TIME.withValue(String.valueOf(context.getExecutionTime())),
                LocationDocumentation.LOCATION_TAG.METHOD_NAME.withValue(String.valueOf(context.getMethodName())),
                LocationDocumentation.LOCATION_TAG.LOCATION_SUCCESSFULLY.withValue(String.valueOf(context.isSuccessful())),
                LocationDocumentation.LOCATION_TAG.THROWABLE.withValue(String.valueOf(context.getThrowable() == null ? "" : context.getThrowable().getMessage()))
        );
    }
}