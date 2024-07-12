/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.opensabe.mapstruct.core;

import org.mapstruct.*;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A type to be used as {@link Context} parameter to track cycles in graphs.
 * <p>
 * Depending on the actual use case, the two methods below could also be changed to only accept certain argument types,
 * e.g. base classes of graph nodes, avoiding the need to capture any other objects that wouldn't necessarily result in
 * cycles.
 *
 * @author Andreas Gudian
 */

@SuppressWarnings("unused")
public class CycleAvoidingMappingContext {
    private final Map<Object, Object> knownInstances = new IdentityHashMap<>();
    @BeforeMapping
    @SuppressWarnings("unchecked")
    public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
        return (T) knownInstances.get( source );
    }

    @BeforeMapping
    public void storeMappedInstance(Object source, @MappingTarget Object target) {
        knownInstances.put( source, target );
    }

    @AfterMapping
    public void clearMappedInstance(Object source, @MappingTarget Object target) {
        knownInstances.remove(source);
    }


}
