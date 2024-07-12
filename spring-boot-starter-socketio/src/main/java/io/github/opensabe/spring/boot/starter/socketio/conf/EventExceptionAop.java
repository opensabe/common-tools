package io.github.opensabe.spring.boot.starter.socketio.conf;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.google.common.collect.Maps;
import io.github.opensabe.spring.boot.starter.socketio.annotation.EventExceptionHandler;
import io.github.opensabe.spring.boot.starter.socketio.annotation.EventExceptionHandlerAdvice;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.*;

@Log4j2
@Aspect
public class EventExceptionAop {

    ApplicationContext applicationContext;
    Map<String, Pair<Object, Method>> CACHE = Maps.newConcurrentMap();

    @Pointcut("@annotation(com.corundumstudio.socketio.annotation.OnEvent)")
    public void exceptionSocketIoAnnotation() {
    }

    public EventExceptionAop(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        getEventExceptionHandlers();
    }

    @Order(1)
    @Around("exceptionSocketIoAnnotation()")
    public Object aroundSocketIoExceptionAnnotationPointCut(ProceedingJoinPoint pjp) throws Throwable {
        Pair<SocketIOClient, Pair<AckRequest, List<Object>>> pair = getArgsFromJoinPoint(pjp);
        List<Object> args = pair.getValue().getValue();
        UUID sessionId = pair.getKey().getSessionId();
        log.info("{}#{},sessionId={},args={}", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), sessionId, JSON.toJSONString(args));
        return pjp.proceed();
    }

    @Order(1)
    @AfterThrowing(value = "exceptionSocketIoAnnotation()", throwing = "ex")
    public void afterThrowingSocketIoExceptionAnnotationPointCut(JoinPoint pjp, Throwable ex) {
        Pair<SocketIOClient, Pair<AckRequest, List<Object>>> pair = getArgsFromJoinPoint(pjp);
        SocketIOClient client = pair.getKey();
        AckRequest request = pair.getValue().getKey();
        List<Object> args = pair.getValue().getValue();

        Pair<Object, Method> adviceToMethod = getEventExceptionHandler(ex);
        if (Objects.nonNull(adviceToMethod)) {
            Method method = adviceToMethod.getValue();
            try {
                method.invoke(ex,adviceToMethod.getKey(), client, request, args.toArray());
            } catch (Throwable e) {
                log.error("EventExceptionAop.afterThrowingSocketIoExceptionAnnotationPointCut: execute eventExceptionHandler error,target={},method={},e={},",
                        adviceToMethod.getKey().getClass().getName(), method.getName(), e.getMessage(), e);
            }
        }
    }

    @Order(1)
    @AfterReturning(value = "exceptionSocketIoAnnotation()", returning = "returnData")
    public void afterReturningSocketIoExceptionAnnotationPointCut(JoinPoint pjp, Object returnData) {
        log.info("{}#{},returnData={}", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), JSON.toJSONString(returnData));
    }

    private Pair<Object, Method> getEventExceptionHandler(Object ex) {
        return CACHE.get(getExceptionName(ex.getClass()));
    }

    private void getEventExceptionHandlers() {
        Map<String, Object> map = this.applicationContext.getBeansWithAnnotation(EventExceptionHandlerAdvice.class);
        map.values().stream()
                .sorted(Comparator.comparingInt(advice -> getClassOrderValue(advice.getClass())))
                .forEach(advice -> {
                    Arrays.stream(advice.getClass().getMethods())
                            .filter(method -> Objects.nonNull(AnnotatedElementUtils.findMergedAnnotation(method, EventExceptionHandler.class)))
                            .sorted(Comparator.comparingInt(method -> getMethodOrderValue(method)))
                            .forEach(method -> {
                                EventExceptionHandler ann = AnnotatedElementUtils.findMergedAnnotation(method, EventExceptionHandler.class);
                                if (!Objects.equals(ann.value().length, 0)) {
                                    Arrays.stream(ann.value()).forEach(ex -> CACHE.computeIfAbsent(getExceptionName(ex), k -> Pair.of(advice, method)));
                                }

                            });

                });
    }

    private Pair<SocketIOClient, Pair<AckRequest, List<Object>>> getArgsFromJoinPoint(JoinPoint pjp) {
        SocketIOClient client = null;
        AckRequest request = null;
        Object[] objects = pjp.getArgs();
        List<Object> args = new ArrayList<>(objects.length);
        for (Object object : objects) {
            if (object instanceof SocketIOClient) {
                client = (SocketIOClient) object;
            } else if (object instanceof AckRequest) {
                request = (AckRequest) object;
            } else {
                args.add(object);
            }
        }
        if (Objects.isNull(client) || Objects.isNull(request)) {
            throw new RuntimeException("eventExceptionHandler method must have parameters: SocketIOClient and AckRequest");
        }
        return Pair.of(client, Pair.of(request, args));
    }

    private String getExceptionName(Class clazz) {
        return clazz.getPackageName() + '#' + clazz.getName();
    }

    private Integer getClassOrderValue(Class clazz) {
        Order annotation = AnnotatedElementUtils.findMergedAnnotation(clazz, Order.class);
        return Objects.nonNull(annotation) ? annotation.value() : Integer.MAX_VALUE;
    }

    private Integer getMethodOrderValue(Method method) {
        Order annotation = method.getAnnotation(Order.class);
        return Objects.nonNull(annotation) ? annotation.value() : Integer.MAX_VALUE;
    }
}
