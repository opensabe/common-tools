package io.github.opensabe.alive.client.callback;

import io.github.opensabe.alive.protobuf.Message;
import java.util.Set;

/**
 * @author lone
 */
public interface ClientCallback {

    void opComplete(Set<Message.Response> response);
}
