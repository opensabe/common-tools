package io.github.opensabe.alive.client.impl.future;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.opensabe.alive.client.Response;
import io.github.opensabe.alive.client.ResponseFuture;
import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;

public class BaseResponseFutureMultiImpl implements BaseResponseFuture {

    private List<ResponseFuture> futureList = new LinkedList<ResponseFuture>();

    public BaseResponseFutureMultiImpl() {
    }

    public void add(ResponseFuture future) {
        futureList.add(future);
    }

    @Override
    public Response get0() throws InterruptedException, AliveClientExecutionException {
        Response ans = Response.FAIL;
        for (ResponseFuture future : futureList) {
            Response response = future.get();
            if (response == Response.CACHED && ans != Response.SUCEESS) {
                ans = Response.CACHED;
            } else if (response == Response.SUCEESS) {
                ans = Response.SUCEESS;
            }
        }
        return ans;
    }

    @Override
    public Response get0(long timeout, TimeUnit unit)
        throws InterruptedException, AliveClientExecutionException, AliveClientTimeoutException {
        if (timeout <= 0) {
            return get0();
        } else {
            Response ans = Response.FAIL;
            long end = System.currentTimeMillis() + unit.toMillis(timeout);
            for (ResponseFuture future : futureList) {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0) {
                    throw new AliveClientTimeoutException();
                } else {
                    Response response = future.get(timeout, TimeUnit.MILLISECONDS);
                    if (response == Response.CACHED && ans != Response.SUCEESS) {
                        ans = Response.CACHED;
                    } else if (response == Response.SUCEESS) {
                        ans = Response.SUCEESS;
                    }
                }
            }
            return ans;
        }
    }
}
