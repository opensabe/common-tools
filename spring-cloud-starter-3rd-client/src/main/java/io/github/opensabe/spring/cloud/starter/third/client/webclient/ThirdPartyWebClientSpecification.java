package io.github.opensabe.spring.cloud.starter.third.client.webclient;

import org.springframework.cloud.context.named.NamedContextFactory;

import java.util.Arrays;

public class ThirdPartyWebClientSpecification implements NamedContextFactory.Specification {

    private final String name;

    private final Class<?>[] configuration;

    public ThirdPartyWebClientSpecification(String name, Class<?>[] configuration) {
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
