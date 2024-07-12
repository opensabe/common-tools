package io.github.opensabe.common.mybatis.monitor;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

/*
 * @author lyq
 * @description monitor transaction
 *
 */
@Log4j2
@Aspect
public class MonitorTransactionAspect {

    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)") // annotation declare pointcut
    public void annotationPointcut() {
    }

    @Before("annotationPointcut()")
    public void beforeMethod(JoinPoint point) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        Transactional annotation = method.getAnnotation(Transactional.class);
        if (annotation != null) {
            //whether the current method is in a transaction
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.debug("The current method stays in the transaction correctly:{}, methodName:{}", actualTransactionActive, method.getName());

            if (!actualTransactionActive
                    && !Propagation.NEVER.equals(annotation.propagation())
                    && !Propagation.NOT_SUPPORTED.equals(annotation.propagation())

            ) {
                log.fatal("Please pay attention: it seems this method is not in a db transaction unintentionally. methodName = {}", method.getName());
            }
        }
    }
}
