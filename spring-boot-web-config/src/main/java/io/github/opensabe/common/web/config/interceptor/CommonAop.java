package io.github.opensabe.common.web.config.interceptor;

import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Log4j2
@Aspect
@Component
public class CommonAop {
    
    @Value(value = "${log.http-request.max-length:200000000}")
    private Integer maxLengthForlogRequest;
    
    public CommonAop() {
    }

    @Pointcut("within(io.github.opensabe..controller..*) && @within(org.springframework.web.bind.annotation.RestController) ")
    public void feignClientMethodPointcut() {
    }

    @Pointcut("execution(* io.github.opensabe..*(..)) && @within(org.springframework.cloud.openfeign.FeignClient)"
    )
    public void restControllerMethodPointcut() {
    }

    @Order(Integer.MIN_VALUE)
    @Around("feignClientMethodPointcut()")
    public Object aroundRPCServicesPointcut(ProceedingJoinPoint pjp) throws Throwable {
        return aroundHTTPMethod(pjp);
    }

    @Order(Integer.MIN_VALUE)
    @Around("restControllerMethodPointcut()")
    public Object aroundRestControllerPointcut(ProceedingJoinPoint pjp) throws Throwable {
        return aroundHTTPMethod(pjp);
    }

    private Object aroundHTTPMethod(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = pjp.getTarget().toString();//获取被拦截的类
        Method method = signature.getMethod(); //获取被拦截的方法
        String methodName = method.getName(); //获取被拦截的方法名
        String name = className + "." + methodName;
        if (log.isInfoEnabled()) {
            StringBuilder stringBuilder = new StringBuilder();
            Object[] args = pjp.getArgs();
            for (Object arg : args) {
                stringBuilder.append(arg == null ? "null" : arg.toString()).append("\t|\t");
            }
            log.info("start to call [{}] with args: [{}]", () -> name, () -> {
                String argsString = stringBuilder.toString();
                return argsString.length() > maxLengthForlogRequest ? argsString.substring(0, maxLengthForlogRequest).concat("....") : argsString;
            });
        }
        Object proceed = pjp.proceed();
        log.info("Successfully called [{}] with return: [{}]", () -> name, () -> {
            String result = JsonUtil.toJSONString(proceed);
            return result.length() > maxLengthForlogRequest ? result.substring(0, maxLengthForlogRequest).concat("....") : result;
        });
        return proceed;
    }
}
