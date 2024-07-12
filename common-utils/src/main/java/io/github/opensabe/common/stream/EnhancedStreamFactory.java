package io.github.opensabe.common.stream;

import java.lang.reflect.Proxy;
import java.util.stream.Stream;

public class EnhancedStreamFactory {
    @SuppressWarnings("unchecked")
    public static <E> EnhancedStream<E> newEnhancedStream(Stream<E> stream) {
        return (EnhancedStream<E>) Proxy.newProxyInstance(
                //必须用EnhancedStream的classLoader，不能用Stream的，因为Stream是jdk的类，ClassLoader是rootClassLoader
                EnhancedStream.class.getClassLoader(),
                //代理接口
                new Class<?>[] {EnhancedStream.class},
                //代理类
                new EnhancedStreamHandler<>(stream)
        );
    }
}
