package io.github.opensabe.alive.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 在发送消息时在前面加上消息长度。
 * 使用定长的int（4字节）来表示。
 */
@Sharable
public class Int32FrameEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int bodyLen = msg.readableBytes();
        out.ensureWritable(4 + bodyLen);
        out.writeInt(bodyLen);
        out.writeBytes(msg, msg.readerIndex(), bodyLen);
    }
}
