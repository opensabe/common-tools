package io.github.opensabe.common.future;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class CompletableFutureWithSpan<T> extends CompletableFuture<T> {
    private final CompletableFuture<T> completableFuture;
    private final Observation observation;

    CompletableFutureWithSpan(CompletableFuture<T> completableFuture, Observation observation) {
        this.completableFuture = completableFuture;
        this.observation = observation;
    }

    private static <T> CompletableFutureWithSpan<T> from(CompletableFuture<T> completableFuture, UnifiedObservationFactory unifiedObservationFactory) {
        return from(
                completableFuture,
                unifiedObservationFactory.getCurrentOrCreateEmptyObservation()
        );
    }

    private static <T> CompletableFutureWithSpan<T> from(CompletableFuture<T> completableFuture, Observation observation) {
        return new CompletableFutureWithSpan<T>(completableFuture, observation);
    }

    public static CompletableFutureWithSpan<Void> allOf(UnifiedObservationFactory unifiedObservationFactory, CompletableFutureWithSpan<?>... cfs) {
        //需要转换
        CompletableFuture[] completableFutures = Arrays.stream(cfs).map(completableFutureWithSpan -> completableFutureWithSpan.completableFuture).collect(Collectors.toList()).toArray(new CompletableFuture[0]);
        return from(CompletableFuture.allOf(completableFutures), unifiedObservationFactory);
    }


    public static CompletableFuture<Object> anyOf(UnifiedObservationFactory unifiedObservationFactory, CompletableFutureWithSpan<?>... cfs) {
        //需要转换
        CompletableFuture[] completableFutures = Arrays.stream(cfs).map(completableFutureWithSpan -> completableFutureWithSpan.completableFuture).collect(Collectors.toList()).toArray(new CompletableFuture[0]);
        return from(CompletableFuture.anyOf(completableFutures), unifiedObservationFactory);
    }

    public static <U> CompletableFutureWithSpan<U> supplyAsync(Supplier<U> supplier, UnifiedObservationFactory unifiedObservationFactory) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        return from(CompletableFuture.supplyAsync(() -> {
            return observation.scoped(supplier);
        }), observation);
    }

    public static <U> CompletableFutureWithSpan<U> supplyAsync(Supplier<U> supplier, UnifiedObservationFactory unifiedObservationFactory, Executor executor) {
        Observation observation = unifiedObservationFactory
                .getCurrentOrCreateEmptyObservation();
        return from(CompletableFuture.supplyAsync(() -> {
            return observation.scoped(supplier);
        }, executor), observation);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, UnifiedObservationFactory unifiedObservationFactory) {
        Observation observation = unifiedObservationFactory
                .getCurrentOrCreateEmptyObservation();
        return from(CompletableFuture.runAsync(() -> {
            observation.scoped(runnable);
        }), observation);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, UnifiedObservationFactory unifiedObservationFactory, Executor executor) {
        Observation observation = unifiedObservationFactory
                .getCurrentOrCreateEmptyObservation();
        return from(CompletableFuture.runAsync(() -> {
            observation.scoped(runnable);
        }, executor), observation);
    }


    @Override
    public <U> CompletableFutureWithSpan<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return from(completableFuture.thenApplyAsync(t -> {
            return observation.scoped(() -> {
                return fn.apply(t);
            });
        }), observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        return from(completableFuture.thenApplyAsync(t -> {
            return observation.scoped(() -> {
                return fn.apply(t);
            });
        }, executor), observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> thenAcceptAsync(Consumer<? super T> action) {
        return from(completableFuture.thenAcceptAsync(t -> {
            observation.scoped(() -> {
                action.accept(t);
            });
        }), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return from(completableFuture.thenAcceptAsync(t -> {
            observation.scoped(() -> {
                action.accept(t);
            });
        }, executor), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> thenRunAsync(Runnable action) {
        return from(completableFuture.thenRunAsync(() -> {
            observation.scoped(action);
        }), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> thenRunAsync(Runnable action, Executor executor) {
        return from(completableFuture.thenRunAsync(() -> {
            observation.scoped(action);
        }, executor), this.observation);
    }

    @Override
    public <U, V> CompletableFutureWithSpan<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return from(completableFuture.thenCombineAsync(other, (t, u) -> {
            return observation.scoped(() -> {
                return fn.apply(t, u);
            });
        }), this.observation);
    }

    @Override
    public <U, V> CompletableFutureWithSpan<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return from(completableFuture.thenCombineAsync(other, (t, u) -> {
            return observation.scoped(() -> {
                return fn.apply(t, u);
            });
        }, executor), this.observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return from(completableFuture.thenAcceptBothAsync(other, (t, u) -> {
            observation.scoped(() -> {
                action.accept(t, u);
            });
        }), this.observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return from(completableFuture.thenAcceptBothAsync(other, (t, u) -> {
            observation.scoped(() -> {
                action.accept(t, u);
            });
        }, executor), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return from(completableFuture.runAfterBothAsync(other, () -> {
            observation.scoped(action);
        }), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return from(completableFuture.runAfterBothAsync(other, () -> {
            observation.scoped(action);
        }, executor), this.observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return from(completableFuture.applyToEitherAsync(other, t -> {
            return observation.scoped(() -> {
                return fn.apply(t);
            });
        }), this.observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return from(completableFuture.applyToEitherAsync(other, t -> {
            return observation.scoped(() -> {
                return fn.apply(t);
            });
        }, executor), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return from(completableFuture.acceptEitherAsync(other, t -> {
            observation.scoped(() -> {
                action.accept(t);
            });
        }), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        return from(completableFuture.acceptEitherAsync(other, t -> {
            observation.scoped(() -> {
                action.accept(t);
            });
        }, executor), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return from(completableFuture.runAfterEitherAsync(other, () -> {
            observation.scoped(action);
        }), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return from(completableFuture.runAfterEitherAsync(other, () -> {
            observation.scoped(action);
        }, executor), this.observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return from(completableFuture.thenComposeAsync(t -> {
            return observation.scoped(() -> {
                return fn.apply(t);
            });
        }), this.observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        return from(completableFuture.thenComposeAsync(t -> {
            return observation.scoped(() -> {
                return fn.apply(t);
            });
        }, executor), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return from(completableFuture.whenCompleteAsync((t, throwable) -> {
            observation.scoped(() -> {
                action.accept(t, throwable);
            });
        }), this.observation);
    }

    @Override
    public CompletableFutureWithSpan<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return from(completableFuture.whenCompleteAsync((t, throwable) -> {
            observation.scoped(() -> {
                action.accept(t, throwable);
            });
        }, executor), this.observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return from(completableFuture.handleAsync((t, throwable) -> {
            return observation.scoped(() -> {
                return fn.apply(t, throwable);
            });
        }), this.observation);
    }

    @Override
    public <U> CompletableFutureWithSpan<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return from(completableFuture.handleAsync((t, throwable) -> {
            return observation.scoped(() -> {
                return fn.apply(t, throwable);
            });
        }, executor), this.observation);
    }

    @Override
    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
        return completableFuture.completeAsync(() -> {
            return observation.scoped(supplier);
        }, executor);
    }

    @Override
    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
        return completableFuture.completeAsync(() -> {
            return observation.scoped(supplier);
        });
    }

    @Override
    public boolean isDone() {
        return completableFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return completableFuture.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(timeout, unit);
    }

    @Override
    public T join() {
        return completableFuture.join();
    }

    @Override
    public T getNow(T valueIfAbsent) {
        return completableFuture.getNow(valueIfAbsent);
    }

    @Override
    public boolean complete(T value) {
        return completableFuture.complete(value);
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        return completableFuture.completeExceptionally(ex);
    }

    @Override
    public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return completableFuture.thenApply(fn);
    }

    @Override
    public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return completableFuture.thenAccept(action);
    }

    @Override
    public CompletableFuture<Void> thenRun(Runnable action) {
        return completableFuture.thenRun(action);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return completableFuture.thenCombine(other, fn);
    }

    @Override
    public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return completableFuture.thenAcceptBoth(other, action);
    }

    @Override
    public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return completableFuture.runAfterBoth(other, action);
    }

    @Override
    public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return completableFuture.applyToEither(other, fn);
    }

    @Override
    public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return completableFuture.acceptEither(other, action);
    }

    @Override
    public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return completableFuture.runAfterEither(other, action);
    }

    @Override
    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return completableFuture.thenCompose(fn);
    }

    @Override
    public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return completableFuture.whenComplete(action);
    }

    @Override
    public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return completableFuture.handle(fn);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return completableFuture.toCompletableFuture();
    }

    @Override
    public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return completableFuture.exceptionally(fn);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return completableFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return completableFuture.isCancelled();
    }

    @Override
    public boolean isCompletedExceptionally() {
        return completableFuture.isCompletedExceptionally();
    }

    @Override
    public void obtrudeValue(T value) {
        completableFuture.obtrudeValue(value);
    }

    @Override
    public void obtrudeException(Throwable ex) {
        completableFuture.obtrudeException(ex);
    }

    @Override
    public int getNumberOfDependents() {
        return completableFuture.getNumberOfDependents();
    }

    @Override
    public String toString() {
        return completableFuture.toString();
    }

    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
        return completableFuture.newIncompleteFuture();
    }

    @Override
    public Executor defaultExecutor() {
        return completableFuture.defaultExecutor();
    }

    @Override
    public CompletableFuture<T> copy() {
        return completableFuture.copy();
    }

    @Override
    public CompletionStage<T> minimalCompletionStage() {
        return completableFuture.minimalCompletionStage();
    }

    @Override
    public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
        return completableFuture.orTimeout(timeout, unit);
    }

    @Override
    public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
        return completableFuture.completeOnTimeout(value, timeout, unit);
    }


}
