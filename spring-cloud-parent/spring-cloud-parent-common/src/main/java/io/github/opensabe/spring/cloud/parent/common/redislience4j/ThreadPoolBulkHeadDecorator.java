package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;

public interface ThreadPoolBulkHeadDecorator {
    ThreadPoolBulkhead decorate(ThreadPoolBulkhead threadPoolBulkhead);
}
