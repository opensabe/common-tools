package io.github.opensabe.spring.boot.starter.socketio.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface EventExceptionHandler {

    Class<? extends Throwable>[] value() default {};
}
