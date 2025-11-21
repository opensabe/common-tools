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

@Log4j2
@Aspect
public class ExceptionHandlerObservationAop {
    private final UnifiedObservationFactory unifiedObservationFactory;

    public ExceptionHandlerObservationAop(UnifiedObservationFactory unifiedObservationFactory) {
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.ExceptionHandler) "
    )
    public void exceptionHandler() {
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Around("exceptionHandler()")
    public Object aroundSocketIoAnnotationPointCut(ProceedingJoinPoint pjp) throws Throwable {
        //Skip recording observation for GexceptionHandler (indicating parameter validation failure) so that the request is not marked as failed
        if (pjp.getThis() instanceof GexceptionHandler) {
            return pjp.proceed();
        }
        //由于我们将异常已经 catch 住了，这样从 observation 看就没有异常，这不是我们想要的
        //我们想要的是就算 catch 住并且返回的是 200，也要在 observation 看到有异常
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
