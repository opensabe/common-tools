package io.github.opensabe.alive.client.impl.future;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.github.opensabe.alive.client.Response;
import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;

public class BaseResponseFutureImpl extends FutureTask<Response> implements BaseResponseFuture {

    public BaseResponseFutureImpl() {
        super(new Callable<Response>() {

            @Override
            public Response call() throws Exception {
                return null;
            }
        });
    }

    @Override
    public void set(Response resp) {
        super.set(resp);
    }

    @Override
    public void setException(Throwable t) {
        super.setException(t);
    }

    @Override
    public Response get0() throws InterruptedException, AliveClientExecutionException {
        try {
            return super.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AliveClientExecutionException) {
                throw (AliveClientExecutionException) e.getCause();
            } else {
                throw new AliveClientExecutionException("Unknow client execution exception.", e.getCause());
            }
        }
    }

    @Override
    public Response get0(long timeout, TimeUnit unit)
        throws InterruptedException, AliveClientExecutionException, AliveClientTimeoutException {
        if (timeout <= 0) {
            return get0();
        }
        try {
            return super.get(timeout, unit);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AliveClientExecutionException) {
                throw (AliveClientExecutionException) e.getCause();
            } else {
                throw new AliveClientExecutionException("Unknow client execution exception.", e.getCause());
            }
        } catch (TimeoutException e) {
            throw new AliveClientTimeoutException();
        }
    }
}
