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
package io.github.opensabe.alive.client.impl;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.opensabe.alive.client.Client;
import io.github.opensabe.alive.client.Response;
import io.github.opensabe.alive.client.ResponseFuture;
import io.github.opensabe.alive.client.callback.ClientCallback;
import io.github.opensabe.alive.client.exception.AliveClientException;
import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;
import io.github.opensabe.alive.client.impl.future.BaseResponseFutureImpl;
import io.github.opensabe.alive.client.impl.future.ResponseFutureImpl;
import io.github.opensabe.alive.client.vo.MessageVo;
import io.github.opensabe.alive.client.vo.QueryVo;
import io.github.opensabe.alive.protobuf.Message;

/**
 * AbstractClient 类。
 * <p>Abstract客户端。</p>
 */
public abstract class AbstractClient implements Client {

/** 产品代码。 */
    protected final int productCode;

/** authToken 字段。 */
    protected final String authToken;

/** 连接超时。 */
    protected final long connectTimeout;

/** authTimeout 字段。 */
    protected final long authTimeout;

    public AbstractClient(int productCode, String authToken, long connectTimeout, long authTimeout) {
        this.productCode = productCode;
        this.authToken = authToken;
        if (connectTimeout == 0) {
            this.connectTimeout = ClientConstants.DEFAULT_TIMEOUT;
        } else {
            this.connectTimeout = connectTimeout;
        }

        if (authTimeout == 0) {
            this.authTimeout = ClientConstants.DEFAULT_TIMEOUT;
        } else {
            this.authTimeout = connectTimeout;
        }
    }

/**
 * query 方法。
 */
    @Override
    public Response query(QueryVo queryVo) throws AliveClientExecutionException, InterruptedException, AliveClientException {
        return query(queryVo, 0, TimeUnit.SECONDS);
    }

/**
 * query 方法。
 */
    @Override
    public Response query(QueryVo queryVo, long timeout, TimeUnit unit)
            throws AliveClientExecutionException, InterruptedException, AliveClientException {
        return queryAsync(queryVo).get();
    }

/**
 * queryAsync 方法。
 */
    @Override
    public ResponseFuture queryAsync(QueryVo queryVo) throws AliveClientException {
        final BaseResponseFutureImpl baseFuture = new BaseResponseFutureImpl();
        ResponseFuture future = new ResponseFutureImpl(baseFuture);
        queryAsync(queryVo, new ClientCallback() {
/**
 * 推送完成回调。
 */
            @Override
            public void opComplete(Set<Message.Response> response) {
                if (response == null || response.size() == 0) {
                    baseFuture.set(Response.ERR);
                } else {
                    for (Message.Response resp : response) {
                        if (resp.getRetCode() == Message.RetCode.SUCCESS) {
                            baseFuture.set(Response.SUCEESS);
                            return;
                        } else if (resp.getRetCode() == Message.RetCode.FAIL) {
                            baseFuture.set(Response.FAIL);
                            return;
                        }
                    }
                    baseFuture.set(Response.ERR);
                }
            }
        });
        return future;
    }


/**
 * push 方法。
 */
    @Override
    public Response push(MessageVo messageVo)
            throws AliveClientExecutionException, AliveClientTimeoutException, InterruptedException, AliveClientException {
        return push(messageVo, 0, TimeUnit.SECONDS);
    }

/**
 * push 方法。
 */
    @Override
    public Response push(MessageVo messageVo, long timeout, TimeUnit unit)
            throws AliveClientExecutionException, AliveClientTimeoutException, InterruptedException, AliveClientException {
        return pushAsync(messageVo).get(timeout, unit);
    }
}
