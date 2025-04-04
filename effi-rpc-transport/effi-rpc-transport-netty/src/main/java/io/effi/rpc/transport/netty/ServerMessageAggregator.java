package io.effi.rpc.transport.netty;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.RepackagedResponse;
import io.effi.rpc.transport.TransportSupport;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import static io.effi.rpc.common.exception.PredefinedErrorCode.CHANNEL_READ;
import static io.effi.rpc.common.exception.PredefinedErrorCode.CHANNEL_WRITE;

/**
 * Handle message aggregation for server-side communication.
 * - Decodes inbound network messages into Request objects.
 * - Encodes outbound Response objects into network messages.
 */
@Sharable
public class ServerMessageAggregator extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerMessageAggregator.class);

    public static final String NAME = "serverMessageAggregator";

    /**
     * Decodes inbound messages into Request objects and triggers request events.
     *
     * @param ctx Netty context.
     * @param msg Inbound message.
     * @throws Exception if decoding fails.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Envelope.Request request) {
            NettyChannel channel = NettyChannel.acquire(ctx.channel());
            TransportSupport.handleRequest(request, channel);
        } else {
            logger.warn(Messages.onlySupport(Envelope.Request.class));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        EffiRpcException exception = CHANNEL_READ.fail(cause, ctx.channel().remoteAddress());
        logger.error(exception);
    }

    /**
     * Encodes Response objects into outbound messages before writing to the channel.
     *
     * @param ctx     Netty context.
     * @param msg     Outbound message.
     * @param promise Write operation promise.
     * @throws Exception if encoding or writing fails.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RepackagedResponse<?> repackagedResponse) {
            promise.addListener(future -> {
                if (!future.isSuccess()) {
                    EffiRpcException exception = CHANNEL_WRITE.fail(future.cause(), ctx.channel().remoteAddress());
                    logger.error(exception);
                }
            });
            super.write(ctx, repackagedResponse.encode().response(), promise);
        } else {
            logger.warn(Messages.onlySupport(RepackagedResponse.class));
        }
    }
}

