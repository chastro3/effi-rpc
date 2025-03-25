package io.effi.rpc.support.handler;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ReplyFuture;
import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.support.AbstractCaller;
import io.effi.rpc.transport.NettyChannel;
import io.effi.rpc.transport.RequestWrapper;
import io.effi.rpc.transport.ResponseWrapper;
import io.effi.rpc.transport.codec.ClientCodec;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Handle message conversion for client-side communication.
 * - Decodes inbound network messages into Response objects.
 * - Encodes outbound Request objects into network messages.
 */
@Sharable
public class ClientMessageAggregator extends ChannelDuplexHandler {

    public static final String NAME = "clientFullMessageAggregator";

    private static final Logger logger = LoggerFactory.getLogger(ClientMessageAggregator.class);

    /**
     * Decodes inbound messages into Response objects and triggers response events.
     *
     * @param ctx Netty context.
     * @param msg Inbound message.
     * @throws Exception if decoding fails.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Envelope.Response response) {
            NettyChannel channel = NettyChannel.acquire(ctx.channel());
            ReplyFuture future = ReplyFuture.acquireFuture(response.url());
            if (future != null && future.context().invoker() instanceof AbstractCaller<?> abstractCaller) {
                ClientCodec clientCodec = abstractCaller.protocolInstance().clientCodec();
                ThreadPool threadPool = abstractCaller.threadPool();
                try {
                    if (abstractCaller.inIODeserialization()) {
                        ResponseWrapper<Caller<?>> responseWrapper = clientCodec.decode(channel, response);
                        threadPool.execute(() -> future.complete(responseWrapper.context()));
                    } else {
                        threadPool.execute(() -> future.complete(clientCodec.decode(channel, response).context()));
                    }
                } catch (Exception e) {
                    EffiRpcException exception = EffiRpcException.wrap(
                            PredefinedErrorCode.CHANNEL_READ,
                            e, channel.url().address());
                    threadPool.execute(() -> future.complete(exception));
                }
            } else {
                logger.warn(Messages.onlySupport(Envelope.Response.class));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        EffiRpcException exception = EffiRpcException.wrap(
                PredefinedErrorCode.CHANNEL_READ,
                cause, ctx.channel().remoteAddress()
        );
        logger.error(exception);
    }

    /**
     * Encodes Request objects into outbound messages before writing to the channel.
     *
     * @param ctx     Netty context.
     * @param msg     Outbound message.
     * @param promise Write operation promise.
     * @throws Exception if encoding or writing fails.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        NettyChannel channel = NettyChannel.acquire(ctx.channel());
        if (msg instanceof RequestWrapper<?> wrapper) {
            registerFailedListener(promise, channel, ReplyFuture.acquireFuture(wrapper.request().url()));
            super.write(ctx, wrapper.encode(channel).request(), promise);
        } else {
            logger.warn(Messages.onlySupport(RequestWrapper.class));
        }
    }

    private void registerFailedListener(ChannelPromise promise,
                                        NettyChannel channel,
                                        ReplyFuture replyFuture) {
        promise.addListener(future -> {
            if (!future.isSuccess()) {
                if (replyFuture != null) {
                    EffiRpcException exception = EffiRpcException.wrap(
                            PredefinedErrorCode.CHANNEL_WRITE,
                            future.cause(),
                            channel.url().address()
                    );
                    replyFuture.context().invoker()
                            .threadPool()
                            .execute(() -> replyFuture.complete(exception));
                }
            }
        });
    }
}


