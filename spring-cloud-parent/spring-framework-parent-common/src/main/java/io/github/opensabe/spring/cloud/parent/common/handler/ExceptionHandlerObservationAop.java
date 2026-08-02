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
package io.github.opensabe.spring.cloud.parent.common.handler;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;

/**
 * 环绕 {@code @ExceptionHandler} 的 Observation 切面：将已捕获的异常写入当前 Observation，
 * 使 HTTP 200 但业务异常的场景仍在链路/指标中体现失败。
 * <p>
 * 对 {@link GexceptionHandler}（参数校验等预期失败）跳过记录，避免误标请求失败。
 */
@Log4j2
@Aspect
public class ExceptionHandlerObservationAop {
    private final UnifiedObservationFactory unifiedObservationFactory;

    /**
     * @param unifiedObservationFactory 延迟初始化的 Observation 工厂
     */
    public ExceptionHandlerObservationAop(UnifiedObservationFactory unifiedObservationFactory) {
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    /** 匹配所有 {@code @ExceptionHandler} 方法。 */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.ExceptionHandler) "
    )
    public void exceptionHandler() {
    }

    /**
     * 在异常处理器执行前，将 handler 参数中的 {@link Throwable} 写入当前 Observation。
     *
     * @param pjp 切点
     * @return handler 返回值
     * @throws Throwable 原 handler 异常
     */
    @Order(Ordered.LOWEST_PRECEDENCE)
    @Around("exceptionHandler()")
    public Object aroundSocketIoAnnotationPointCut(ProceedingJoinPoint pjp) throws Throwable {
        if (pjp.getThis() instanceof GexceptionHandler) {
            return pjp.proceed();
        }
        Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
        if (currentObservation != null) {
            Object[] args = pjp.getArgs();
            if (args != null) {
                for (Object arg : args) {

                    if (arg instanceof Throwable throwable) {
                        currentObservation.error(throwable);
                        break;
                    }
                }
            }
        }
        return pjp.proceed();
    }
}
