/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package io.github.opensabe.spring.boot.starter.socketio.tracing.extend;

@FunctionalInterface
public interface MultiConsumer<N,S,L,A> {

    void accept(N n,S s,L l,A a);


}
