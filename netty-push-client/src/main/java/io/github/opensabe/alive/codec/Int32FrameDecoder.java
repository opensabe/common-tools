package io.github.opensabe.alive.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 通过给定的消息长度来解析数据包
 * 长度部分是个4字节int
 */
public class Int32FrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();
        if (!in.isReadable() || in.readableBytes() < 4) {
            in.resetReaderIndex();
            return;
        }
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        } else {
            out.add(in.readBytes(length));
            return;
        }
    }
}
