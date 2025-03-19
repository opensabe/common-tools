package io.github.opensabe.spring.cloud.parent.web.common.handler;


import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.handler.IException;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

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
        //由于我们将异常已经 catch 住了，这样从 observation 看就没有异常，这不是我们想要的
        //我们想要的是就算 catch 住并且返回的是 200，也要在 observation 看到有异常
        Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
        if (currentObservation != null) {
            Object[] args = pjp.getArgs();
            if (args != null) {
                for (Object arg : args) {
                    //IException是参数校验失败，这时候不认为请求失败了
                    if (arg instanceof Throwable throwable && !(arg instanceof IException)) {
                        currentObservation.error(throwable);
                        break;
                    }
                }
            }
        }
        return pjp.proceed();
    }
}
