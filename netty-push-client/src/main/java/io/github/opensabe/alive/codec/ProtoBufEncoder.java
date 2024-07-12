package io.github.opensabe.alive.codec;

import com.google.protobuf.GeneratedMessageV3;
import io.github.opensabe.alive.protobuf.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtoBufEncoder extends MessageToByteEncoder<GeneratedMessageV3> {

    @Override
    protected void encode(ChannelHandlerContext ctx, GeneratedMessageV3 message, ByteBuf out) throws Exception {
        String className = message.getClass().getName();
        short methodId = MessageType.requestNameToMethodId.get(className);
        out.writeShort(methodId);
        out.writeBytes(message.toByteArray());
    }

}
