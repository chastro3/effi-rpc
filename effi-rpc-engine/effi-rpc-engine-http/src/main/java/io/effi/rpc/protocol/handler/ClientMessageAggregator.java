package io.effi.rpc.protocol.handler;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ReplyFuture;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.DefaultRepackagedRequest;
import io.effi.rpc.transport.RepackagedRequest;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.netty.NettyChannel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import static io.effi.rpc.common.exception.PredefinedErrorCode.CHANNEL_READ;
import static io.effi.rpc.common.exception.PredefinedErrorCode.CHANNEL_WRITE;

/**
 * Handle message conversion for client-side communication.
 * - Decodes inbound network messages into Response objects.
 * - Encodes outbound Request objects into network messages.
 */
@Sharable
public class ClientMessageAggregator extends ChannelDuplexHandler {

    public static final String NAME = "clientFullMessageAggregator";

    private static final Logger logger = LoggerFactory.getLogger(ClientMessageAggregator.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Envelope.Response response) {
            NettyChannel channel = NettyChannel.acquire(ctx.channel());
            TransportSupport.receiveResponse(response, channel);
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
        if (msg instanceof RepackagedRequest<?> marshalingRequest) {
            // todo 优化
            registerFailedListener(promise, marshalingRequest.channel(), ReplyFuture.getFuture(marshalingRequest.request().url()));
            super.write(ctx, marshalingRequest.encode().request(), promise);
        } else {
            logger.warn(Messages.onlySupport(DefaultRepackagedRequest.class));
        }
    }

    private void registerFailedListener(ChannelPromise promise,
                                        Channel channel,
                                        ReplyFuture replyFuture) {
        promise.addListener(future -> {
            if (!future.isSuccess()) {
                if (replyFuture != null) {
                    EffiRpcException exception = CHANNEL_WRITE.fail(future.cause(), channel.remoteAddress());
                    replyFuture.context().invoker()
                            .threadPool()
                            .execute(() -> replyFuture.complete(exception));
                }
            }
        });
    }
}


