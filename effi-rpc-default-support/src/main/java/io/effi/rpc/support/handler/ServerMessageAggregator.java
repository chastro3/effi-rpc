package io.effi.rpc.support.handler;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.support.AbstractCallee;
import io.effi.rpc.transport.NettyChannel;
import io.effi.rpc.transport.RequestWrapper;
import io.effi.rpc.transport.ResponseWrapper;
import io.effi.rpc.transport.codec.ServerCodec;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Handle message aggregation for server-side communication.
 * - Decodes inbound network messages into Request objects.
 * - Encodes outbound Response objects into network messages.
 */
@Sharable
public class ServerMessageAggregator extends ChannelDuplexHandler {

    public static final String NAME = "serverFullMessageConverter";

    private static final Logger logger = LoggerFactory.getLogger(ServerMessageAggregator.class);

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
            Callee<?> callee = channel.module()
                    .serverExporterManager()
                    .acquireCallee(request.url());
            // todo send to client
            if (callee == null) {
                throw EffiRpcException.wrap(PredefinedErrorCode.NOT_FOUND_CALLEE, request.url().uri());
            }
            if (callee instanceof AbstractCallee<?> abstractCallee) {
                ServerCodec serverCodec = abstractCallee.protocolInstance().serverCodec();
                ThreadPool threadPool = abstractCallee.threadPool(channel.module());
                try {
                    if (abstractCallee.inIODeserialization()) {
                        RequestWrapper<Callee<?>> requestWrapper = serverCodec.decode(channel, request);
                        threadPool.execute(() -> invokeAndSend(channel, requestWrapper, abstractCallee, serverCodec));
                    } else {
                        threadPool.execute(() -> {
                            RequestWrapper<Callee<?>> requestWrapper = serverCodec.decode(channel, request);
                            invokeAndSend(channel, requestWrapper, abstractCallee, serverCodec);
                        });
                    }

                } catch (Exception e) {
                    throw EffiRpcException.wrap(PredefinedErrorCode.CHANNEL_READ, e, channel.url().address());
                }
            }
        } else {
            logger.warn(Messages.onlySupport(Envelope.Request.class));
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
     * Encodes Response objects into outbound messages before writing to the channel.
     *
     * @param ctx     Netty context.
     * @param msg     Outbound message.
     * @param promise Write operation promise.
     * @throws Exception if encoding or writing fails.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        NettyChannel channel = NettyChannel.acquire(ctx.channel());
        if (msg instanceof ResponseWrapper<?> wrapper) {
            promise.addListener(future -> {
                if (!future.isSuccess()) {
                    EffiRpcException exception = EffiRpcException.wrap(
                            PredefinedErrorCode.CHANNEL_WRITE,
                            future.cause(),
                            ctx.channel().remoteAddress()
                    );
                    logger.error(exception);
                }
            });
            super.write(ctx, wrapper.encode(channel).response(), promise);
        } else {
            logger.warn(Messages.onlySupport(ResponseWrapper.class));
        }
    }

    private void invokeAndSend(NettyChannel channel,
                               RequestWrapper<Callee<?>> requestWrapper,
                               AbstractCallee<?> callee,
                               ServerCodec serverCodec) {
        ReplyContext<Envelope.Response, Callee<?>> replyContext = callee.invokeWithContext(requestWrapper.context());
        ResponseWrapper<Callee<?>> responseWrapper = new ResponseWrapper<>(replyContext, serverCodec);
        if (requestWrapper.request().needReply()) {
            responseWrapper = callee.inIOSerialization()
                    ? responseWrapper
                    : responseWrapper.encode(channel);
            channel.send(responseWrapper);
        }

    }
}

