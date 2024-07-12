package io.github.opensabe.common.alive.client;


import io.github.opensabe.common.alive.client.message.MessageVo;

public interface Client {

    int pushAsync(MessageVo messageVo, ClientCallback callback);

}