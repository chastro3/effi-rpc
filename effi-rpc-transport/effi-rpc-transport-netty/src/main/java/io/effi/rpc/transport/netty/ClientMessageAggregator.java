package io.effi.rpc.transport.netty;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.DefaultWrappedRequest;
import io.effi.rpc.transport.WrappedRequest;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.LazyInitializer;
import io.effi.rpc.util.Messages;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import static io.effi.rpc.exception.PredefinedErrorCode.CHANNEL_READ;
import static io.effi.rpc.exception.PredefinedErrorCode.CHANNEL_WRITE;

/**
 * Handle message conversion for client-side communication.
 * - Decodes inbound network messages into Response objects.
 * - Encodes outbound Request objects into network messages.
 */
@Sharable
public class ClientMessageAggregator extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientMessageAggregator.class);

    private static final LazyInitializer<NamedChannelHandler> LAZY_INITIALIZER = new LazyInitializer<>(
            () -> new NamedChannelHandler("clientMessageAggregator", new ClientMessageAggregator())
    );

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Envelope.Response response) {
            NettyChannel channel = NettyChannel.get(ctx.channel());
            TransportSupport.handleResponse(response, channel);
        } else {
            logger.warn(Messages.onlySupport(Envelope.Response.class));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        EffiRpcException exception = CHANNEL_READ.fail(cause, ctx.channel().remoteAddress());
        logger.error(exception);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof WrappedRequest<?> wrappedRequest) {
            addFailedListener(promise, wrappedRequest.channel(), ReplyFuture.getFuture(wrappedRequest.request().url()));
            super.write(ctx, wrappedRequest.encode().request(), promise);
        } else if (msg instanceof Envelope.Request request) {
            addFailedListener(promise, NettyChannel.get(ctx.channel()), ReplyFuture.getFuture(request.url()));
            super.write(ctx, msg, promise);
        } else {
            logger.warn(Messages.onlySupport(DefaultWrappedRequest.class));
        }
    }

    private void addFailedListener(ChannelPromise promise, Channel channel, ReplyFuture replyFuture) {
        promise.addListener(future -> {
            if (!future.isSuccess()) {
                if (replyFuture != null) {
                    EffiRpcException exception = CHANNEL_WRITE.fail(future.cause(), channel.remoteAddress());
                    logger.error(exception.getMessage());
                    replyFuture.context().invoker().threadPool().execute(() -> replyFuture.complete(exception));
                } else {
                    logger.warn("ReplyFuture is null, cannot complete with exception.");
                }
            }
        });
    }

    public static NamedChannelHandler getInstance() {
        return LAZY_INITIALIZER.get(false);
    }
}


