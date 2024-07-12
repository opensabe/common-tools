package io.github.opensabe.common.s3.service;

@FunctionalInterface
public interface Provider<T> {

    T supply();
}
