package io.github.opensabe.common.jfr;

import io.micrometer.observation.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class JFRObservationHandlerTest {

    @Mock
    private ObservationToJFRGenerator<TestContext> generator1;
    
    @Mock
    private ObservationToJFRGenerator<TestContext> generator2;
    
    @Mock
    private ObservationToJFRGenerator<AnotherContext> generator3;

    private JFRObservationHandler<TestContext> handler;
    private TestContext testContext;
    private AnotherContext anotherContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(generator1.getContextClazz()).thenReturn(TestContext.class);
        when(generator2.getContextClazz()).thenReturn(TestContext.class);
        when(generator3.getContextClazz()).thenReturn(AnotherContext.class);
        
        List<ObservationToJFRGenerator<TestContext>> generators = Arrays.asList(generator1, generator2);
        handler = new JFRObservationHandler<>(generators);
        
        testContext = new TestContext();
        anotherContext = new AnotherContext();
    }

    @Test
    void onStart_ShouldCallAllMatchingGenerators() {
        // when
        handler.onStart(testContext);

        // then
        verify(generator1).onStart(testContext);
        verify(generator2).onStart(testContext);
        verify(generator3, never()).onStart(any());
    }

    @Test
    void onStart_ShouldHandleGeneratorException() {
        // given
        doThrow(new RuntimeException("Test exception")).when(generator1).onStart(testContext);

        // when
        handler.onStart(testContext);

        // then
        verify(generator1).onStart(testContext);
        verify(generator2).onStart(testContext);
    }

    @Test
    void onStop_ShouldCallAllMatchingGenerators() {
        // when
        handler.onStop(testContext);

        // then
        verify(generator1).onStop(testContext);
        verify(generator2).onStop(testContext);
        verify(generator3, never()).onStop(any());
    }

    @Test
    void onStop_ShouldHandleGeneratorException() {
        // given
        doThrow(new RuntimeException("Test exception")).when(generator1).onStop(testContext);

        // when
        handler.onStop(testContext);

        // then
        verify(generator1).onStop(testContext);
        verify(generator2).onStop(testContext);
    }

    @Test
    void supportsContext_ShouldAlwaysReturnTrue() {
        assert handler.supportsContext(testContext);
        assert handler.supportsContext(anotherContext);
    }

    @Test
    void constructor_ShouldInitializeEmptyMap_WhenNoGenerators() {
        // when
        JFRObservationHandler<TestContext> emptyHandler = new JFRObservationHandler<>(Collections.emptyList());

        // then
        emptyHandler.onStart(testContext);
        emptyHandler.onStop(testContext);
        // No exceptions should be thrown
    }

    private static class TestContext extends Observation.Context {}
    private static class AnotherContext extends Observation.Context {}
} 