/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.boot.starter.socketio.tracing.observation;

import java.util.List;
import java.util.function.Consumer;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.transport.NamespaceClient;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.boot.starter.socketio.tracing.EventEnum;
import io.github.opensabe.spring.boot.starter.socketio.tracing.extend.MultiConsumer;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;

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
