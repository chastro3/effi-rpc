package io.effi.rpc.transport.netty;

import io.effi.rpc.context.Request;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.transport.ServerRequestHandler;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.message.EncodableOutputMessage;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;
import io.effi.rpc.util.ExceptionUtil;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.Messages;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Converts messages for server-side communication: decodes inbound requests and
 * encodes outbound responses.
 */
@NativeConfig.Reflect(typeReached = NettyChannel.class, queryAllPublicMethods = true)
@Sharable
public final class ServerMessageAggregator extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerMessageAggregator.class);

    private static final LazySingleton<NamedChannelHandler> LAZY_INITIALIZER =
            LazySingleton.from(() -> new NamedChannelHandler("serverMessageAggregator", new ServerMessageAggregator()));

    private final ServerRequestHandler requestHandler = new ServerRequestHandler();

    private ServerMessageAggregator() {
    }

    /**
     * Returns the shared server-side channel handler.
     */
    public static NamedChannelHandler getInstance() {
        return LAZY_INITIALIZER.ensure();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof InputMessage inputMessage) {
            requestHandler.handle(inputMessage);
        } else {
            logger.warn(Messages.onlySupport(Request.class));
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
        addFailedListener(ctx.channel(), msg, promise);
        if (msg instanceof EncodableOutputMessage<?> outputMessage) {
            super.write(ctx, outputMessage.encode(), promise);
        } else if (msg instanceof OutputMessage outputMessage) {
            super.write(ctx, outputMessage, promise);
        } else {
            logger.warn(Messages.onlySupport(OutputMessage.class));
        }
    }

    private void addFailedListener(Channel channel, Object msg, ChannelPromise promise) {
        if (msg instanceof OutputMessage) {
            promise.addListener(future -> {
                if (!future.isSuccess()) {
                    EffiRpcException exception = TransportErrorCodes.CHANNEL_WRITE.fail(future.cause(), channel.remoteAddress());
                    logger.error(exception);
                }
            });
        }
    }

}

