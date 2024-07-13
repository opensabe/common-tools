package io.github.opensabe.spring.cloud.parent.web.common.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.spec.HttpServletResponseImpl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class HttpServletResponseImplUtil {
    private static final MethodHandles.Lookup lookup;
    private static final VarHandle exchangeHandle;

    static {
        try {
            lookup = MethodHandles.privateLookupIn(HttpServletResponseImpl.class, MethodHandles.lookup());
            exchangeHandle = lookup.findVarHandle(HttpServletResponseImpl.class, "exchange", HttpServerExchange.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpServerExchange getExchange(HttpServletResponseImpl response) {
        return (HttpServerExchange) exchangeHandle.get(response);
    }
}
