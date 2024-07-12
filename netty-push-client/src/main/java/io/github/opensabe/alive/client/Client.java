package io.github.opensabe.alive.client;

import java.util.concurrent.TimeUnit;

import io.github.opensabe.alive.client.callback.ClientCallback;
import io.github.opensabe.alive.client.exception.AliveClientException;
import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;
import io.github.opensabe.alive.client.vo.MessageVo;
import io.github.opensabe.alive.client.vo.QueryVo;


public interface Client {

    /**
     * 同步请求查询对应设备的相关主题是否还在缓存
     *
     * @param queryVo 消息
     * @return Response 还在缓存返回SUCESS 不在缓存返回FAIL
     * @throws AliveClientExecutionException 等待过程中发现异常返回
     * @throws InterruptedException 等待过程中被打断抛出
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    Response query(QueryVo queryVo) throws AliveClientExecutionException, InterruptedException, AliveClientException;

    /**
     * 同步请求查询对应设备的相关主题是否还在缓存
     *
     * @param queryVo 消息
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return Response 还在缓存返回SUCESS 不在缓存返回FAIL
     * @throws AliveClientTimeoutException 等待超时抛出
     * @throws AliveClientExecutionException 等待过程中发现异常返回
     * @throws InterruptedException 等待过程中被打断抛出
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    Response query(QueryVo queryVo, long timeout, TimeUnit unit)
        throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException;

    /**
     * 同步请求查询对应设备的相关主题是否还在缓存
     *
     * @param queryVo 消息
     * @return ResponseFuture 还在缓存返回SUCESS 不在缓存返回FAIL AliveClientExecutionException 等待过程中发现异常返回 InterruptedException 等待过程中被打断抛出
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    ResponseFuture queryAsync(QueryVo queryVo) throws AliveClientException;

    /**
     * 异步请求
     *
     * @param queryVo 消息
     * @param callback 回调函数,任务完成后调用
     * @return int requestId
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    int queryAsync(QueryVo queryVo, ClientCallback callback) throws AliveClientException;


    /**
     * 同步请求
     *
     * @param messageVo 消息
     * @return Response
     * @throws AliveClientTimeoutException 等待超时抛出
     * @throws AliveClientExecutionException 等待过程中发现异常返回
     * @throws InterruptedException 等待过程中被打断抛出
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    Response push(MessageVo messageVo)
        throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException;


    /**
     * 同步请求，带超时时间
     *
     * @param messageVo 消息
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return Response
     * @throws AliveClientTimeoutException 等待超时抛出
     * @throws AliveClientExecutionException 等待过程中发现异常返回
     * @throws InterruptedException 等待过程中被打断抛出
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    Response push(MessageVo messageVo, long timeout, TimeUnit unit)
        throws AliveClientTimeoutException, AliveClientExecutionException, InterruptedException, AliveClientException;

    /**
     * 异步请求
     *
     * @param messageVo 消息
     * @return ResponseFuture
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    ResponseFuture pushAsync(MessageVo messageVo) throws AliveClientException;

    /**
     * 异步请求
     *
     * @param messageVo 消息
     * @param callback 回调函数,任务完成后调用
     * @return int requestId
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    int pushAsync(MessageVo messageVo, ClientCallback callback) throws AliveClientException;

    /**
     * 关闭客户端，回收资源
     *
     * @throws AliveClientException 请求是发生异常直接抛出
     */
    void close() throws AliveClientException;

}
