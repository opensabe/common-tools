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
 * Invokes {@link GracefulShutdownHandler} beans during application shutdown.
 * <p>
 * Boot 4 removed Undertow's post-drain {@code ShutdownListener}. This bean is a
 * {@link SmartLifecycle} with phase below Boot's
 * {@code WebServerGracefulShutdownLifecycle.SMART_LIFECYCLE_PHASE}
 * ({@code Integer.MAX_VALUE - 1024}) so handlers run <b>after</b> the embedded
 * web server has finished graceful drain — matching 2.x Undertow semantics and
 * avoiding {@link org.springframework.context.event.ContextClosedEvent} which
 * fires before {@code lifecycleProcessor.onClose()}.
 */
@Log4j2
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class UndertowGracefulShutdownInitializer implements SmartLifecycle {

	/**
	 * Just below {@code WebServerGracefulShutdownLifecycle.SMART_LIFECYCLE_PHASE}
	 * so we stop after the web server drain (higher phases stop first).
	 */
	public static final int PHASE = Integer.MAX_VALUE - 1024 - 1;

	private final List<GracefulShutdownHandler> gracefulShutdownHandlers;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicBoolean shutdownStarted = new AtomicBoolean(false);

	public UndertowGracefulShutdownInitializer(List<GracefulShutdownHandler> gracefulShutdownHandlers) {
		this.gracefulShutdownHandlers = gracefulShutdownHandlers;
	}

	@Override
	public void start() {
		running.set(true);
	}

	@Override
	public void stop() {
		runHandlers();
		running.set(false);
	}

	@Override
	public void stop(Runnable callback) {
		try {
			stop();
		}
		finally {
			callback.run();
		}
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public int getPhase() {
		return PHASE;
	}

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
