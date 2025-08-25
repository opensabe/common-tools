/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
