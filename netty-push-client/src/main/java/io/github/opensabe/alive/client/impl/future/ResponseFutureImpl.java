package io.github.opensabe.alive.client.impl.future;

import java.util.concurrent.TimeUnit;

import io.github.opensabe.alive.client.Response;
import io.github.opensabe.alive.client.ResponseFuture;
import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;

public class ResponseFutureImpl implements ResponseFuture {

    private BaseResponseFuture future;

    public ResponseFutureImpl(BaseResponseFuture future) {
        this.future = future;
    }

    private Response get0() throws InterruptedException, AliveClientExecutionException {
        return future.get0();
    }

    private Response get0(long timeout, TimeUnit unit)
        throws InterruptedException, AliveClientExecutionException, AliveClientTimeoutException {
        return future.get0(timeout, unit);
    }

    @Override
    public Response get() throws InterruptedException, AliveClientExecutionException {
        return get0();
    }

    @Override
    public Response get(long timeout, TimeUnit unit)
        throws InterruptedException, AliveClientExecutionException, AliveClientTimeoutException {
        return get0(timeout, unit);
    }

    @Override
    public Response getUninterruptibly() throws AliveClientExecutionException {
        while (true) {
            try {
                return get0();
            } catch (InterruptedException e) {

            }
        }
    }

    @Override
    public Response getUninterruptibly(long timeout, TimeUnit unit) throws AliveClientExecutionException, AliveClientTimeoutException {
        if (timeout == 0) {
            return getUninterruptibly();
        }
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        while (true) {
            timeout = end - System.currentTimeMillis();
            if (timeout <= 0) {
                throw new AliveClientTimeoutException();
            }
            try {
                return get0(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {

            }
        }
    }

}
