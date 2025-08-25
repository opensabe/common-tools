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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import static io.github.opensabe.alive.client.Response.SUCEESS;

import io.github.opensabe.alive.client.Client;
import io.github.opensabe.alive.client.Response;
import io.github.opensabe.alive.client.ResponseFuture;
import io.github.opensabe.alive.client.callback.ClientCallback;
import io.github.opensabe.alive.client.exception.AliveClientException;
import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;
import io.github.opensabe.alive.client.impl.future.BaseResponseFutureImpl;
import io.github.opensabe.alive.client.impl.future.ResponseFutureImpl;
import io.github.opensabe.alive.client.vo.MQTopic;
import io.github.opensabe.alive.client.vo.MessageVo;
import io.github.opensabe.alive.client.vo.QueryVo;
import io.github.opensabe.alive.protobuf.Message;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MQClientImpl implements Client {
    @Setter
    private RocketMQTemplate producer;
    private Integer productCode;
    private AtomicInteger requestId = new AtomicInteger(1);

    public MQClientImpl(RocketMQTemplate producer, Integer productCode) {
        this.producer = producer;
        this.productCode = productCode;
    }

    @Override
    public Response query(QueryVo queryVo) throws AliveClientExecutionException, InterruptedException, AliveClientException {
        throw new AliveClientException("not supported action");
    }

    @Override
    public Response query(QueryVo queryVo, long timeout, TimeUnit unit) throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException {
        throw new AliveClientException("not supported action");
    }

    @Override
    public ResponseFuture queryAsync(QueryVo queryVo) throws AliveClientException {
        throw new AliveClientException("not supported action");
    }

    @Override
    public int queryAsync(QueryVo queryVo, ClientCallback callback) throws AliveClientException {
        throw new AliveClientException("not supported action");
    }

    @Override
    public Response push(MessageVo messageVo) throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException {
        producer.syncSend(getTopic(messageVo), build(messageVo));
        return SUCEESS;
    }

    @Override
    public Response push(MessageVo messageVo, long timeout, TimeUnit unit) throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException {
        producer.syncSend(getTopic(messageVo), build(messageVo));
        return SUCEESS;
    }

    @Override
    public ResponseFuture pushAsync(MessageVo messageVo) throws AliveClientException {
        var f = new BaseResponseFutureImpl();
        producer.asyncSend(getTopic(messageVo), build(messageVo), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                f.set(SUCEESS);
            }

            @Override
            public void onException(Throwable throwable) {
                f.setException(throwable);
            }
        });
        return new ResponseFutureImpl(f);
    }

    @Override
    public int pushAsync(MessageVo messageVo, ClientCallback callback) throws AliveClientException {
        var message = build(messageVo);
        producer.asyncSend(getTopic(messageVo), message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                callback.opComplete(Set.of(Message.Response.newBuilder()
                        .setRetCode(Message.RetCode.SUCCESS)
                        .setRequestId(message.getRequestId())
                        .build()));
            }

            @Override
            public void onException(Throwable throwable) {
                log.error(throwable);
                callback.opComplete(Set.of(Message.Response.newBuilder()
                        .setRetCode(Message.RetCode.FAIL)
                        .setRequestId(message.getRequestId())
                        .build()));
            }
        });
        return 0;
    }

    @Override
    public void close() throws AliveClientException {

    }

    private String getTopic(MessageVo message) {
        if (io.github.opensabe.alive.protobuf.Message.PushType.GROUP.equals(message.pushType)) {
            return MQTopic.BROAD_CAST.getTopic();
        }
        return MQTopic.SIMPLE.getTopic();
    }

    private Message.Publish build(MessageVo messageVo) {
        return messageVo.buildPublush(messageVo.getRequestId() == 0 ? requestId.incrementAndGet() : messageVo.getRequestId(),
                productCode);
    }
}
