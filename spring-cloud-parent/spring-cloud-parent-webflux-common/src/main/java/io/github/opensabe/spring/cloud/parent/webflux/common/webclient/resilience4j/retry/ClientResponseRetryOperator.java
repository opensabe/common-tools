package io.github.opensabe.spring.cloud.parent.webflux.common.webclient.resilience4j.retry;

import io.github.resilience4j.reactor.IllegalPublisherException;
import io.github.resilience4j.retry.Retry;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.UnaryOperator;

/**
 * 在官方原始版本的基础上，特定了形参并增加了日志
 * @see io.github.resilience4j.reactor.retry.RetryOperator
 */
@Log4j2
public class ClientResponseRetryOperator implements UnaryOperator<Publisher<ClientResponse>> {
    private final Retry retry;
    private final Observation observation;

    private ClientResponseRetryOperator(Retry retry, Observation observation) {
        this.retry = retry;
        this.observation = observation;
    }

    public static ClientResponseRetryOperator of(Retry retry, Observation observation) {
        return new ClientResponseRetryOperator(retry, observation);
    }

    @Override
    public Publisher<ClientResponse> apply(Publisher<ClientResponse> publisher) {
        if (publisher instanceof Mono) {
            ClientResponseContext clientResponseContext = new ClientResponseContext(retry.asyncContext(), observation);
            Mono<ClientResponse> upstream = (Mono<ClientResponse>) publisher;
            return upstream.doOnNext(clientResponseContext::handleResult)
                    .retryWhen(reactor.util.retry.Retry.withThrowable(errors -> errors.flatMap(clientResponseContext::handleErrors)))
                    .doOnSuccess(t -> clientResponseContext.onComplete());
        } else if (publisher instanceof Flux) {
            ClientResponseContext clientResponseContext = new ClientResponseContext(retry.asyncContext(), observation);
            Flux<ClientResponse> upstream = (Flux<ClientResponse>) publisher;
            return upstream.doOnNext(clientResponseContext::handleResult)
                    .retryWhen(reactor.util.retry.Retry.withThrowable(errors -> errors.flatMap(clientResponseContext::handleErrors)))
                    .doOnComplete(clientResponseContext::onComplete);
        } else {
            throw new IllegalPublisherException(publisher);
        }
    }

    private static class ClientResponseContext {

        private final Retry.AsyncContext<ClientResponse> retryContext;
        private final Observation observation;

        ClientResponseContext(Retry.AsyncContext<ClientResponse> retryContext, Observation observation) {
            this.retryContext = retryContext;
            this.observation = observation;
        }

        void onComplete() {
            this.retryContext.onComplete();
        }

        void handleResult(ClientResponse result) {
            long waitDurationMillis = retryContext.onResult(result);
            if (waitDurationMillis != -1) {
                throw new ClientResponseContext.RetryDueToResultException(waitDurationMillis);
            }
        }

        Publisher<Long> handleErrors(Throwable throwable) {
            return observation.scoped(() -> {
                if (throwable instanceof ClientResponseContext.RetryDueToResultException) {
                    long waitDurationMillis = ((ClientResponseContext.RetryDueToResultException) throwable).waitDurationMillis;
                    log.info("web client retry: got RetryDueToResultException: {}, retry waitDurationMillis: {}", throwable.getLocalizedMessage(), waitDurationMillis);
                    return Mono.delay(Duration.ofMillis(waitDurationMillis));
                }
                // Filter Error to not retry on it
                if (throwable instanceof Error) {
                    log.info("web client retry: will not retry: {}", throwable.toString());
                    throw (Error) throwable;
                }

                long waitDurationMillis = retryContext.onError(throwable);
                log.info("web client retry: got exception: {}, retry waitDurationMillis: {}", throwable.toString(), waitDurationMillis);
                if (waitDurationMillis == -1) {
                    return Mono.error(throwable);
                }

                return Mono.delay(Duration.ofMillis(waitDurationMillis));
            });
        }

        private static class RetryDueToResultException extends RuntimeException {
            private final long waitDurationMillis;

            RetryDueToResultException(long waitDurationMillis) {
                super("retry due to retryOnResult predicate");
                this.waitDurationMillis = waitDurationMillis;
            }
        }
    }
}
