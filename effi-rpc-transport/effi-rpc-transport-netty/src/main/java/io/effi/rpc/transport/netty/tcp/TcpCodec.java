package io.effi.rpc.transport.netty.tcp;

import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Read the Envelope by data length.
 */
public final class TcpCodec {

    private final ChannelHandler encoder;

    private final ChannelHandler decoder;

    private final URL endpointUrl;

    public TcpCodec(URL url, boolean isServer) {
        String maxMessageKey = isServer ? DefaultConfigNames.SERVER_MAX_RECEIVE_SIZE.realName() : DefaultConfigNames.CLIENT_MAX_RECEIVE_SIZE.realName();
        int maxReceiveSize = url.getIntParam(maxMessageKey, Constant.DEFAULT_MAX_MESSAGE_SIZE);
        endpointUrl = url;
        encoder = new NettyEncoder();
        decoder = new NettyDecoder(maxReceiveSize);
    }

    public ChannelHandler encoder() {
        return encoder;
    }

    public ChannelHandler decoder() {
        return decoder;
    }

    public URL endpointUrl() {
        return endpointUrl;
    }

    static class NettyEncoder extends MessageToByteEncoder<Object> {

        @Override
        protected void encode(ChannelHandlerContext ctx, Object message, ByteBuf out) throws Exception {
            if (message instanceof byte[] encodedMessage) {
                out.writeInt(encodedMessage.length);
                out.writeBytes(encodedMessage);
            }
        }
    }

    static class NettyDecoder extends LengthFieldBasedFrameDecoder {

        NettyDecoder(int maxFrameLength) {
            // First int is total length
            super(maxFrameLength, 0, 4, 0, 4, true);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
            if (byteBuf != null) {
                byte[] bytes = NettySupport.getBytes(byteBuf);
                try {
                    return bytes;
                } finally {
                    byteBuf.release();
                }
            }
            return null;
        }
    }

}
