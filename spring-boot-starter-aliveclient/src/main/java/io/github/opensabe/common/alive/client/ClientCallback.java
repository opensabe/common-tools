package io.github.opensabe.common.alive.client;

import io.github.opensabe.common.alive.client.message.Response;

import java.util.Set;

public interface ClientCallback {
    void opComplete(Set<Response> response);
}