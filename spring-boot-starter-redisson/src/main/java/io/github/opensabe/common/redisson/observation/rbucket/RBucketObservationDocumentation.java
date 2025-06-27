package io.github.opensabe.common.redisson.observation.rbucket;

import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * @author heng.ma
 */
public enum RBucketObservationDocumentation implements ObservationDocumentation {
    SET("redisson.bucket.set"),
    GET("redisson.bucket.get"),
    DELETE("redisson.bucket.delete"),
    EXPIRE("redisson.bucket.expire"),
    ;

    private final String name;



    RBucketObservationDocumentation(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<RBucketObservationConvention> getDefaultConvention() {
        return RBucketObservationConvention.class;
    }
}
