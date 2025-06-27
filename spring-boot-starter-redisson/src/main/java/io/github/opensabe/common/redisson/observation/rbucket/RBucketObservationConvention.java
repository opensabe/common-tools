package io.github.opensabe.common.redisson.observation.rbucket;

import io.micrometer.common.KeyValues;
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * @author heng.ma
 */
public class RBucketObservationConvention implements ObservationConvention<RBucketContext> {

    public static final RBucketObservationConvention DEFAULT = new RBucketObservationConvention();

    @Override
    public KeyValues getLowCardinalityKeyValues(RBucketContext context) {
        return KeyValues.of("bucketName", context.getBucketName())
                .and("method", context.getMethod());
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RBucketContext context) {
        KeyValues keyValues = getLowCardinalityKeyValues(context).and("threadName", context.getThreadName());
        if (context.getTtl() != null) {
            keyValues = keyValues.and("ttl", context.getTtl().toString());
        }
        return keyValues;
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RBucketContext;
    }

    public enum TAG implements KeyName {
        BUCKET_NAME {
            @Override
            public String asString() {
                return "redisson.bucket.bucketName";
            }
        },

        METHOD {
            @Override
            public String asString() {
                return "redisson.bucket.method";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.bucket.threadName";
            }
        },
        TTL {
            @Override
            public String asString() {
                return "redisson.bucket.ttl";
            }
        }
        ;


    }
}
