package io.github.opensabe.spring.cloud.parent.web.common.test.feign;

import io.github.opensabe.spring.cloud.parent.web.common.feign.OpenfeignUtil;
import io.github.opensabe.spring.cloud.parent.web.common.feign.RetryableMethod;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

import java.lang.reflect.Method;

/**
 * Util 类静态方法测试
 */
@Execution(ExecutionMode.CONCURRENT)
@Disabled
public class OpenfeignUtilTest {
    public static class SimpleClass {
        public void testSimple() {}
        @RetryableMethod
        public void testAnnotated() {}
    }

    @RetryableMethod
    public static class AnnotatedClass {
        public void testSimple() {}
    }

    @Test
    public void testGetMethod() {
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.httpMethod()).thenReturn(Request.HttpMethod.GET);
        Assertions.assertTrue(OpenfeignUtil.isRetryableRequest(request));
    }

    private Request getPostRequest(Method method) {
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.httpMethod()).thenReturn(Request.HttpMethod.POST);
        RequestTemplate requestTemplate = Mockito.mock(RequestTemplate.class);
        Mockito.when(request.requestTemplate()).thenReturn(requestTemplate);
        MethodMetadata methodMetadata = Mockito.mock(MethodMetadata.class);
        Mockito.when(requestTemplate.methodMetadata()).thenReturn(methodMetadata);
        Mockito.when(methodMetadata.method()).thenReturn(method);
        return request;
    }

    @Test
    public void testPostMethod() throws Exception {
        Request testSimple = getPostRequest(SimpleClass.class.getMethod("testSimple"));
        Assertions.assertFalse(OpenfeignUtil.isRetryableRequest(testSimple));
    }

    @Test
    public void testAnnotatedMethod() throws Exception {
        Request testAnnotated = getPostRequest(SimpleClass.class.getMethod("testAnnotated"));
        Assertions.assertTrue(OpenfeignUtil.isRetryableRequest(testAnnotated));
    }

    @Test
    public void testAnnotatedClass() throws Exception {
        Request testSimple = getPostRequest(AnnotatedClass.class.getMethod("testSimple"));
        Assertions.assertTrue(OpenfeignUtil.isRetryableRequest(testSimple));
    }
}