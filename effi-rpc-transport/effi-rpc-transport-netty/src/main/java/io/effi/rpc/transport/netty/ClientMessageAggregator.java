package io.effi.rpc.transport.netty;

import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Response;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.ClientResponseHandler;
import io.effi.rpc.transport.message.EncodableOutputMessage;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;
import io.effi.rpc.util.ExceptionUtil;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.Messages;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;

/**
 * Handles client-side Netty channel reads and writes for transport messages.
 */
@NativeConfig.Reflect(typeReached = NettyChannel.class, queryAllPublicMethods = true)
@Sharable
public final class ClientMessageAggregator extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientMessageAggregator.class);

    private static final LazySingleton<NamedChannelHandler> LAZY_INITIALIZER = LazySingleton.from(
            () -> new NamedChannelHandler("clientMessageAggregator", new ClientMessageAggregator())
    );

    private final ClientResponseHandler responseHandler = new ClientResponseHandler();

    private ClientMessageAggregator() {
    }

    /**
     * Returns the shared client-side channel handler.
     */
    public static NamedChannelHandler getInstance() {
        return LAZY_INITIALIZER.ensure();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof InputMessage inputMessage) {
            responseHandler.handle(inputMessage);
        } else {
            logger.warn(Messages.onlySupport(Response.class));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        EffiRpcException exception = TransportErrorCodes.CHANNEL_EXCEPTION.fail(
                cause,
                ctx.channel().remoteAddress(),
                ExceptionUtil.message(cause)
        );
        logger.error(exception);
        try {
            super.exceptionCaught(ctx, cause);
        } finally {
            ctx.close();
        }
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
                ReplyFuture replyFuture = ReplyFuture.lookup(
                        outputMessage.channel().platform(),
                        outputMessage.url()
                );
                if (replyFuture != null) {
                    InetSocketAddress remoteAddress = outputMessage.channel().remoteAddress();
                    EffiRpcException exception = TransportErrorCodes.CHANNEL_WRITE.fail(future.cause(), remoteAddress);
                    replyFuture.context().peer().threadPool().execute(() -> replyFuture.failure(exception));
                } else {
                    logger.warn("ReplyFuture is null, cannot complete with exception.");
                }
            }
        });
    }

}


