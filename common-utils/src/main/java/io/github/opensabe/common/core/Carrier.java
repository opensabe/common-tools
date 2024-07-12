package io.github.opensabe.common.core;

import lombok.Getter;
import lombok.Setter;

/**
 * @author musaxi on 2017/11/1.
 */
@Getter
@Setter
public class Carrier<T> {
    private T obj;

    public Carrier(T obj) {
        this.obj = obj;
    }
}