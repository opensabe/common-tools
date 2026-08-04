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

import static io.github.opensabe.alive.client.Response.SUCEESS;

/**
 * MQClientImpl 类。
 * <p>MQClient实现。</p>
 */
@Log4j2
public class MQClientImpl implements Client {
/** producer 字段。 */
    @Setter
    private RocketMQTemplate producer;
/** 产品代码。 */
    private Integer productCode;
/** 请求 ID。 */
    private AtomicInteger requestId = new AtomicInteger(1);

    public MQClientImpl(RocketMQTemplate producer, Integer productCode) {
        this.producer = producer;
        this.productCode = productCode;
    }

/**
 * query 方法。
 */
    @Override
    public Response query(QueryVo queryVo) throws AliveClientExecutionException, InterruptedException, AliveClientException {
        throw new AliveClientException("not supported action");
    }

/**
 * query 方法。
 */
    @Override
    public Response query(QueryVo queryVo, long timeout, TimeUnit unit) throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException {
        throw new AliveClientException("not supported action");
    }

/**
 * queryAsync 方法。
 */
    @Override
    public ResponseFuture queryAsync(QueryVo queryVo) throws AliveClientException {
        throw new AliveClientException("not supported action");
    }

/**
 * queryAsync 方法。
 */
    @Override
    public int queryAsync(QueryVo queryVo, ClientCallback callback) throws AliveClientException {
        throw new AliveClientException("not supported action");
    }

/**
 * push 方法。
 */
    @Override
    public Response push(MessageVo messageVo) throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException {
        producer.syncSend(getTopic(messageVo), build(messageVo));
        return SUCEESS;
    }

/**
 * push 方法。
 */
    @Override
    public Response push(MessageVo messageVo, long timeout, TimeUnit unit) throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException {
        producer.syncSend(getTopic(messageVo), build(messageVo));
        return SUCEESS;
    }

/**
 * 异步推送消息。
 */
    @Override
    public ResponseFuture pushAsync(MessageVo messageVo) throws AliveClientException {
        var f = new BaseResponseFutureImpl();
        producer.asyncSend(getTopic(messageVo), build(messageVo), new SendCallback() {
/**
 * onSuccess 方法。
 */
            @Override
            public void onSuccess(SendResult sendResult) {
                f.set(SUCEESS);
            }

/**
 * onException 方法。
 */
            @Override
            public void onException(Throwable throwable) {
                f.setException(throwable);
            }
        });
        return new ResponseFutureImpl(f);
    }

/**
 * 异步推送消息。
 */
    @Override
    public int pushAsync(MessageVo messageVo, ClientCallback callback) throws AliveClientException {
        var message = build(messageVo);
        producer.asyncSend(getTopic(messageVo), message, new SendCallback() {
/**
 * onSuccess 方法。
 */
            @Override
            public void onSuccess(SendResult sendResult) {
                callback.opComplete(Set.of(Message.Response.newBuilder()
                        .setRetCode(Message.RetCode.SUCCESS)
                        .setRequestId(message.getRequestId())
                        .build()));
            }

/**
 * onException 方法。
 */
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

/**
 * close 方法。
 */
    @Override
    public void close() throws AliveClientException {

    }

/**
 * getTopic 方法。
 */
    private String getTopic(MessageVo message) {
        if (io.github.opensabe.alive.protobuf.Message.PushType.GROUP.equals(message.pushType)) {
            return MQTopic.BROAD_CAST.getTopic();
        }
        return MQTopic.SIMPLE.getTopic();
    }

/**
 * build 方法。
 */
    private Message.Publish build(MessageVo messageVo) {
        return messageVo.buildPublush(messageVo.getRequestId() == 0 ? requestId.incrementAndGet() : messageVo.getRequestId(),
                productCode);
    }
}
