package io.github.opensabe.alive.client;

import java.util.concurrent.TimeUnit;

import io.github.opensabe.alive.client.exception.AliveClientExecutionException;
import io.github.opensabe.alive.client.exception.AliveClientTimeoutException;

public interface ResponseFuture {

    /**
     * 获取响应结果，直到结果返回
     *
     * @return Response
     * @throws InterruptedException 打断时抛出
     * @throws AliveClientExecutionException 执行过程中出现异常抛出
     */
    public Response get() throws InterruptedException, AliveClientExecutionException;

    /**
     * 获取响应结果，可以指定超时时间
     *
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return Response
     * @throws InterruptedException 打断时抛出
     * @throws AliveClientExecutionException 执行过程中出现异常抛出
     * @throws AliveClientTimeoutException 执行超时抛出
     */
    public Response get(long timeout, TimeUnit unit)
        throws InterruptedException, AliveClientExecutionException, AliveClientTimeoutException;

    /**
     * 获取响应结果，忽略打断异常，直到结果返回
     *
     * @return Response
     * @throws AliveClientExecutionException 执行过程中出现异常抛出
     */
    public Response getUninterruptibly() throws AliveClientExecutionException;

    /**
     * 获取响应结果，可以指定超时时间，忽略打断异常，直到结果返回
     *
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return Response
     * @throws AliveClientExecutionException 执行过程中出现异常抛出
     * @throws AliveClientTimeoutException 执行超时抛出
     */
    public Response getUninterruptibly(long timeout, TimeUnit unit) throws AliveClientExecutionException, AliveClientTimeoutException;

}
