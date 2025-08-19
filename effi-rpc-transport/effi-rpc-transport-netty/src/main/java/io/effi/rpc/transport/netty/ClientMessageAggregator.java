package io.effi.rpc.transport.netty;

import io.effi.rpc.context.Response;
import io.effi.rpc.context.support.ReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.message.EncodableOutputMessage;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.Messages;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;

import static io.effi.rpc.exception.PredefinedErrorCode.CHANNEL_READ;
import static io.effi.rpc.exception.PredefinedErrorCode.CHANNEL_WRITE;

/**
 * Converts messages for client-side communication.
 * - Decodes inbound messages into responses.
 * - Encodes outbound requests into messages.
 */
@NativeConfig.Reflect(typeReached = NettyChannel.class, queryAllPublicMethods = true)
@Sharable
public final class ClientMessageAggregator extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientMessageAggregator.class);

    private static final LazySingleton<NamedChannelHandler> LAZY_INITIALIZER = LazySingleton.from(
            () -> new NamedChannelHandler("clientMessageAggregator", new ClientMessageAggregator())
    );

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof InputMessage inputMessage) {
            TransportSupport.handleResponse(inputMessage);
        } else {
            logger.warn(Messages.onlySupport(Response.class));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        EffiRpcException exception = CHANNEL_READ.fail(cause, ctx.channel().remoteAddress());
        logger.error(exception);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof EncodableOutputMessage<?> outputMessage) {
            addFailedListener(promise, outputMessage);
            super.write(ctx, outputMessage.encode(), promise);
        } else if (msg instanceof OutputMessage outputMessage) {
            addFailedListener(promise, outputMessage);
            super.write(ctx, outputMessage, promise);
        } else {
            logger.warn(Messages.onlySupport(OutputMessage.class));
        }
    }

    private void addFailedListener(ChannelPromise promise, OutputMessage outputMessage) {
        promise.addListener(future -> {
            if (!future.isSuccess()) {
                ReplyFuture replyFuture = ReplyFuture.lookup(outputMessage.url());
                if (replyFuture != null) {
                    InetSocketAddress remoteAddress = outputMessage.channel().remoteAddress();
                    EffiRpcException exception = CHANNEL_WRITE.fail(future.cause(), remoteAddress);
                    replyFuture.context().peer().threadPool().execute(() -> replyFuture.failure(exception));
                } else {
                    logger.warn("ReplyFuture is null, cannot complete with exception.");
                }
            }
        });
    }

    public static NamedChannelHandler getInstance() {
        return LAZY_INITIALIZER.ensure();
    }

    private ClientMessageAggregator() {
    }
}


