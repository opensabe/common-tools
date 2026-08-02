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
package io.github.opensabe.spring.cloud.parent.web.common.undertow;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.opensabe.common.executor.GracefulShutdownHandler;
import lombok.extern.log4j.Log4j2;

/**
 * 应用关闭时调用 {@link GracefulShutdownHandler} 的 {@link SmartLifecycle} 实现。
 * <p>
 * Boot 4 移除了 Undertow 排水后的 {@code ShutdownListener}。本 Bean 的生命周期 phase
 * 低于 Boot {@code WebServerGracefulShutdownLifecycle.SMART_LIFECYCLE_PHASE}
 * （{@code Integer.MAX_VALUE - 1024}），因此在嵌入式 Web 服务器完成优雅排水<b>之后</b>才执行，
 * 与 2.x Undertow 语义一致。避免使用 {@link org.springframework.context.event.ContextClosedEvent}，
 * 因其在 {@code lifecycleProcessor.onClose()} 之前触发。
 */
@Log4j2
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class UndertowGracefulShutdownInitializer implements SmartLifecycle {

	/**
	 * 生命周期 phase，略低于 {@code WebServerGracefulShutdownLifecycle.SMART_LIFECYCLE_PHASE}，
	 * 使本组件在 Web 服务器排水完成后再停止（phase 越高越先停止）。
	 */
	public static final int PHASE = Integer.MAX_VALUE - 1024 - 1;

	/**
	 * 待执行的优雅关闭处理器列表。
	 */
	private final List<GracefulShutdownHandler> gracefulShutdownHandlers;

	/**
	 * 标记生命周期是否处于运行状态。
	 */
	private final AtomicBoolean running = new AtomicBoolean(false);

	/**
	 * 保证关闭处理器只执行一次的标志。
	 */
	private final AtomicBoolean shutdownStarted = new AtomicBoolean(false);

	/**
	 * @param gracefulShutdownHandlers 优雅关闭处理器列表
	 */
	public UndertowGracefulShutdownInitializer(List<GracefulShutdownHandler> gracefulShutdownHandlers) {
		this.gracefulShutdownHandlers = gracefulShutdownHandlers;
	}

	/**
	 * 启动生命周期，标记为运行中。
	 */
	@Override
	public void start() {
		running.set(true);
	}

	/**
	 * 停止生命周期并执行所有优雅关闭处理器。
	 */
	@Override
	public void stop() {
		runHandlers();
		running.set(false);
	}

	/**
	 * 带回调的停止：先执行处理器，再通知调用方完成。
	 *
	 * @param callback 停止完成后的回调
	 */
	@Override
	public void stop(Runnable callback) {
		try {
			stop();
		}
		finally {
			callback.run();
		}
	}

	/**
	 * @return 是否处于运行状态
	 */
	@Override
	public boolean isRunning() {
		return running.get();
	}

	/**
	 * @return SmartLifecycle phase，控制与 Web 服务器排水的先后顺序
	 */
	@Override
	public int getPhase() {
		return PHASE;
	}

	/**
	 * 按 {@link Ordered#getOrder()} 升序依次调用所有优雅关闭处理器。
	 * <p>
	 * 使用 {@link AtomicBoolean#compareAndSet} 保证只执行一次；单个处理器异常不会阻断后续处理器。
	 */
	private void runHandlers() {
		if (!shutdownStarted.compareAndSet(false, true)) {
			return;
		}
		log.info("WebServerGracefulShutdown start: gracefulShutdownHandlers size {}",
				gracefulShutdownHandlers.size());
		gracefulShutdownHandlers.stream()
				.sorted(Comparator.comparing(Ordered::getOrder))
				.forEach(handler -> {
					String simpleName = handler.getClass().getSimpleName();
					try {
						log.info("WebServerGracefulShutdown {} start", simpleName);
						handler.gracefullyShutdown();
						log.info("WebServerGracefulShutdown {} end", simpleName);
					}
					catch (Throwable e) {
						log.fatal("shutdown {} error", simpleName, e);
					}
				});
	}
}
