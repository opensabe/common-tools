package io.github.opensabe.spring.boot.starter.socketio.tracing.observation;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.transport.NamespaceClient;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.boot.starter.socketio.tracing.extend.MultiConsumer;
import io.github.opensabe.spring.boot.starter.socketio.tracing.EventEnum;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Log4j2
public class ObservationService {

    private UnifiedObservationFactory unifiedObservationFactory;

    public ObservationService(UnifiedObservationFactory unifiedObservationFactory) {
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    public void observation(SocketIOClient socketIOClient, SocketIOExecuteDocumentation socketIOExecuteDocumentation, String eventName, String annotationName, Consumer<SocketIOClient> consumer) {
        SocketIOExecuteContext context = new SocketIOExecuteContext(socketIOClient, eventName, EventEnum.getInstance(annotationName));
        Observation observation = socketIOExecuteDocumentation.observation(
                        null,
                        socketIOExecuteDocumentation.getConvention(),
                        () -> context,
                        unifiedObservationFactory.getObservationRegistry()).parentObservation(unifiedObservationFactory.getCurrentObservation())
                .start();
        try {
            observation.scoped(() -> consumer.accept(socketIOClient));
        } catch (Throwable e) {
            log.error("OberVationService-observation failed ", e.getMessage(), e);
            observation.error(e);
        } finally {
            observation.stop();
        }
    }

    public void observationEvent(NamespaceClient socketIOClient, SocketIOExecuteDocumentation socketIOExecuteDocumentation, String eventName, String annotationName, List<Object> args, AckRequest ackRequest, MultiConsumer<NamespaceClient, String, List<Object>, AckRequest> consumer) {
        SocketIOExecuteContext context = new SocketIOExecuteContext(socketIOClient, eventName, EventEnum.getInstance(annotationName));
        Observation observation = socketIOExecuteDocumentation.observation(
                        null,
                        socketIOExecuteDocumentation.getConvention(),
                        () -> context,
                        unifiedObservationFactory.getObservationRegistry()).parentObservation(unifiedObservationFactory.getCurrentObservation())
                .start();
        try {
            observation.scoped(() -> consumer.accept(socketIOClient, eventName, args, ackRequest));
        } catch (Throwable e) {
            log.error("OberVationService-observation failed ", e.getMessage(), e);
            observation.error(e);
        } finally {
            observation.stop();
        }
    }
}
