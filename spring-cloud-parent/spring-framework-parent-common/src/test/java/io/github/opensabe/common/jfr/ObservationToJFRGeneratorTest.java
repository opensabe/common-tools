package io.github.opensabe.common.jfr;

import io.micrometer.observation.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservationToJFRGeneratorTest {

    private TestObservationToJFRGenerator generator;
    private TestContext testContext;
    private AnotherContext anotherContext;

    @BeforeEach
    void setUp() {
        generator = new TestObservationToJFRGenerator();
        testContext = new TestContext();
        anotherContext = new AnotherContext();
    }

    @Test
    void onStart_ShouldCallGenerateOnStart_WhenShouldGenerateOnStartReturnsTrue() {
        // given
        generator.setShouldGenerateOnStart(true);

        // when
        generator.onStart(testContext);

        // then
        assertTrue(generator.isGenerateOnStartCalled());
    }

    @Test
    void onStart_ShouldNotCallGenerateOnStart_WhenShouldGenerateOnStartReturnsFalse() {
        // given
        generator.setShouldGenerateOnStart(false);

        // when
        generator.onStart(testContext);

        // then
        assertFalse(generator.isGenerateOnStartCalled());
    }

    @Test
    void onStop_ShouldCallCommitOnStop_WhenShouldCommitOnStopReturnsTrue() {
        // given
        generator.setShouldCommitOnStop(true);

        // when
        generator.onStop(testContext);

        // then
        assertTrue(generator.isCommitOnStopCalled());
    }

    @Test
    void onStop_ShouldNotCallCommitOnStop_WhenShouldCommitOnStopReturnsFalse() {
        // given
        generator.setShouldCommitOnStop(false);

        // when
        generator.onStop(testContext);

        // then
        assertFalse(generator.isCommitOnStopCalled());
    }

    @Test
    void onStart_ShouldThrowClassCastException_WhenContextTypeMismatch() {
        assertThrows(ClassCastException.class, () -> generator.onStart(anotherContext));
    }

    @Test
    void onStop_ShouldThrowClassCastException_WhenContextTypeMismatch() {
        assertThrows(ClassCastException.class, () -> generator.onStop(anotherContext));
    }

    @Test
    void getContextClazz_ShouldReturnCorrectClass() {
        assertEquals(TestContext.class, generator.getContextClazz());
    }

    private static class TestContext extends Observation.Context {}
    private static class AnotherContext extends Observation.Context {}

    private static class TestObservationToJFRGenerator extends ObservationToJFRGenerator<TestContext> {
        private boolean shouldGenerateOnStart;
        private boolean shouldCommitOnStop;
        private boolean generateOnStartCalled;
        private boolean commitOnStopCalled;

        @Override
        public Class<TestContext> getContextClazz() {
            return TestContext.class;
        }

        @Override
        protected boolean shouldCommitOnStop(TestContext context) {
            return shouldCommitOnStop;
        }

        @Override
        protected boolean shouldGenerateOnStart(TestContext context) {
            return shouldGenerateOnStart;
        }

        @Override
        protected void commitOnStop(TestContext context) {
            commitOnStopCalled = true;
        }

        @Override
        protected void generateOnStart(TestContext context) {
            generateOnStartCalled = true;
        }

        public void setShouldGenerateOnStart(boolean shouldGenerateOnStart) {
            this.shouldGenerateOnStart = shouldGenerateOnStart;
        }

        public void setShouldCommitOnStop(boolean shouldCommitOnStop) {
            this.shouldCommitOnStop = shouldCommitOnStop;
        }

        public boolean isGenerateOnStartCalled() {
            return generateOnStartCalled;
        }

        public boolean isCommitOnStopCalled() {
            return commitOnStopCalled;
        }
    }
} 