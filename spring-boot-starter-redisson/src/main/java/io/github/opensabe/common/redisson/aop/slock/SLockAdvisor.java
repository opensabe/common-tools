package io.github.opensabe.common.redisson.aop.slock;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * @author heng.ma
 */
@RequiredArgsConstructor
public class SLockAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private final SLockPointcut pointcut;

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
}
