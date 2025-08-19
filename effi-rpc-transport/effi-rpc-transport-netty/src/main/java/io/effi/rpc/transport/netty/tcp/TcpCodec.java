package io.effi.rpc.transport.netty.tcp;

import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.SmartURL;
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

    private final SmartURL endpointSmartUrl;

    public TcpCodec(SmartURL smartUrl, boolean isServer) {
        String maxMessageKey = isServer ? ConfigNames.SERVER_MAX_RECEIVE_SIZE.name() : ConfigNames.CLIENT_MAX_RECEIVE_SIZE.name();
        int maxReceiveSize = Integer.parseInt(smartUrl.getQueryParam(maxMessageKey));
        endpointSmartUrl = smartUrl;
        encoder = new NettyEncoder();
        decoder = new NettyDecoder(maxReceiveSize);
    }

    public ChannelHandler encoder() {
        return encoder;
    }

    public ChannelHandler decoder() {
        return decoder;
    }

    public SmartURL endpointUrl() {
        return endpointSmartUrl;
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
