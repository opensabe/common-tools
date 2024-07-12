package io.github.opensabe.alive.client.impl.future;

import java.util.concurrent.TimeUnit;

import io.github.opensabe.alive.client.Response;
import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;

public interface BaseResponseFuture {

    public Response get0() throws InterruptedException, AliveClientExecutionException;

    public Response get0(long timeout, TimeUnit unit)
        throws InterruptedException, AliveClientExecutionException, AliveClientTimeoutException;

}
