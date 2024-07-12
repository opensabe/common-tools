package io.github.opensabe.common.mybatis.jfr;

import io.github.opensabe.common.mybatis.observation.ConnectionContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;

/**
 * mysql连接池上报JFR
 * @author maheng
 */
public class ConnectionJFRGenerator extends ObservationToJFRGenerator<ConnectionContext> {
    @Override
    public Class<ConnectionContext> getContextClazz() {
        return ConnectionContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(ConnectionContext context) {
        return context.containsKey(ConnectionEvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(ConnectionContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(ConnectionContext context) {
        ConnectionEvent event = context.get(ConnectionEvent.class);
        event.commit();
    }

    @Override
    protected void generateOnStart(ConnectionContext context) {
        ConnectionEvent event = new ConnectionEvent(context);
        context.put(ConnectionEvent.class, event);
        event.begin();
    }
}
