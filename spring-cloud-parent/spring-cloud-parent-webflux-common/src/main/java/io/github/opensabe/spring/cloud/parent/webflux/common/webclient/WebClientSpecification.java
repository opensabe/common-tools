package io.github.opensabe.spring.cloud.parent.webflux.common.webclient;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.cloud.context.named.NamedContextFactory;

import java.util.Arrays;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class WebClientSpecification implements NamedContextFactory.Specification {

    private final String name;

    private final Class<?>[] configuration;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public WebClientSpecification(String name, Class<?>[] configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return "WebClientSpecification{" +
                "name='" + name + '\'' +
                ", configuration=" + Arrays.toString(configuration) +
                '}';
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?>[] getConfiguration() {
        return configuration;
    }
}
