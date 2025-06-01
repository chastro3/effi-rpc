package io.effi.rpc.transport.netty;

import io.effi.rpc.base.Envelope;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.WrappedResponse;
import io.effi.rpc.util.LazyInitializer;
import io.effi.rpc.util.Messages;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import static io.effi.rpc.exception.PredefinedErrorCode.CHANNEL_READ;
import static io.effi.rpc.exception.PredefinedErrorCode.CHANNEL_WRITE;

/**
 * Converts messages for client-side communication.
 * - Decodes inbound messages into response.
 * - Encodes outbound request into messages.
 */
@Sharable
public final class ServerMessageAggregator extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerMessageAggregator.class);

    private static final LazyInitializer<NamedChannelHandler> LAZY_INITIALIZER = new LazyInitializer<>(() -> new NamedChannelHandler("serverMessageAggregator", new ServerMessageAggregator()));

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Envelope.Request request) {
            NettyChannel channel = NettyChannel.get(ctx.channel());
            if (channel != null) TransportSupport.handleRequest(request, channel);
        } else {
            logger.warn(Messages.onlySupport(Envelope.Request.class));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        EffiRpcException exception = CHANNEL_READ.fail(cause, ctx.channel().remoteAddress());
        logger.error(exception);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        addFailedListener(ctx.channel(), msg, promise);
        if (msg instanceof WrappedResponse<?> wrappedResponse) {
            super.write(ctx, wrappedResponse.encode().response(), promise);
        } else if (msg instanceof Envelope.Response) {
            super.write(ctx, msg, promise);
        } else {
            logger.warn(Messages.onlySupport(WrappedResponse.class));
        }
    }

    private void addFailedListener(Channel channel, Object msg, ChannelPromise promise) {
        if (msg instanceof WrappedResponse<?> || msg instanceof Envelope.Response) {
            promise.addListener(future -> {
                if (!future.isSuccess()) {
                    EffiRpcException exception = CHANNEL_WRITE.fail(future.cause(), channel.remoteAddress());
                    logger.error(exception);
                }
            });
        }
    }

    public static NamedChannelHandler getInstance() {
        return LAZY_INITIALIZER.get(false);
    }

    private ServerMessageAggregator() {

    }
}

