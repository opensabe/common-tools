package io.github.opensabe.spring.cloud.parent.web.common.undertow;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.opensabe.common.utils.AlarmUtil;
import io.undertow.server.handlers.GracefulShutdownHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Comparator;
import java.util.List;

@Log4j2
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class UndertowGracefulShutdownInitializer implements ApplicationListener<WebServerInitializedEvent> {
    private static VarHandle undertowGracefulShutdown;
    private static VarHandle undertowShutdownListeners;

    static {
        try {
            undertowGracefulShutdown = MethodHandles
                    .privateLookupIn(UndertowWebServer.class, MethodHandles.lookup())
                    .findVarHandle(UndertowWebServer.class, "gracefulShutdown",
                            GracefulShutdownHandler.class);
            undertowShutdownListeners = MethodHandles
                    .privateLookupIn(GracefulShutdownHandler.class, MethodHandles.lookup())
                    .findVarHandle(GracefulShutdownHandler.class, "shutdownListeners",
                            List.class);
        } catch (Throwable e) {
            AlarmUtil.fatal("UndertowGracefulShutdownInitializer undertow not found, ignore fetch var handles");
        }
    }

    private final List<UndertowGracefulShutdownHandler> undertowGracefulShutdownHandlers;

    public UndertowGracefulShutdownInitializer(List<UndertowGracefulShutdownHandler> undertowGracefulShutdownHandlers) {
        this.undertowGracefulShutdownHandlers = undertowGracefulShutdownHandlers;
    }


    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        WebServer webServer = event.getWebServer();
        if (webServer instanceof UndertowWebServer) {
            GracefulShutdownHandler gracefulShutdownHandler = (GracefulShutdownHandler) undertowGracefulShutdown.getVolatile(webServer);
            if (gracefulShutdownHandler != null) {
                var shutdownListeners = (List<GracefulShutdownHandler.ShutdownListener>) undertowShutdownListeners.getVolatile(gracefulShutdownHandler);
                shutdownListeners.add(shutdownSuccessful -> {
                    if (shutdownSuccessful) {
                        log.info("UndertowGracefulShutdown start to shutdown: undertowGracefulShutdownHandlers size {}", undertowGracefulShutdownHandlers.size());
                        undertowGracefulShutdownHandlers.stream()
                                .sorted(Comparator.comparing(Ordered::getOrder))
                                .forEach(undertowGracefulShutdownHandler -> {
                                    String simpleName = undertowGracefulShutdownHandler.getClass().getSimpleName();
                                    try {
                                        log.info("UndertowGracefulShutdown {} start", simpleName);
                                        undertowGracefulShutdownHandler.gracefullyShutdown();
                                        log.info("UndertowGracefulShutdown {} end", simpleName);
                                    } catch (Throwable e) {
                                        log.fatal("shutdown {} error", simpleName, e);
                                    }
                                });
                    } else {
                        log.fatal("graceful shutdown shutdownSuccessful is not true");
                    }
                });
            }
        } else {
            log.fatal("UndertowGracefulShutdownInitializer-onApplicationEvent, current is not undertow {}", webServer);
        }
    }
}
