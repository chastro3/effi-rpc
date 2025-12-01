package io.effi.rpc.transport.netty;

import io.effi.rpc.component.event.EventDispatcher;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.effi.rpc.transport.idle.IdleEvent;
import io.effi.rpc.transport.idle.RefreshIdleCountEvent;
import io.effi.rpc.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Detects idle states in Netty channels and publishes related events.
 * <p>
 * Provides idle detection functionality for Netty channels by monitoring
 * channel activity and publishing {@link IdleEvent} when channels become inactive,
 * as well as {@link RefreshIdleCountEvent} when channels receive read activity.
 */
@NativeConfig.Reflect(typeReached = NettyChannel.class, queryAllPublicMethods = true)
@Sharable
public class IdleDetectionHandler extends ChannelInboundHandlerAdapter {

    private final Endpoint endpoint;

    public IdleDetectionHandler(Endpoint endpoint) {
        this.endpoint = AssertUtil.notNull(endpoint, "endpoint");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        String thisName = ctx.name();
        pipeline.addBefore(thisName, "idleStateHandler", newIdleStateHandler());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyChannel channel = NettyChannel.ensure(ctx.channel());
        endpoint.platform().singleComponent(EventDispatcher.class).publish(new RefreshIdleCountEvent(channel));
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        NettyChannel nettyChannel = NettyChannel.ensure(ctx.channel());
        if (evt instanceof IdleStateEvent event && event.state() == IdleState.ALL_IDLE) {
            Optional.ofNullable(nettyChannel.get(KeyConstant.IDLE_COUNT))
                    .ifPresent(AtomicInteger::incrementAndGet);
            endpoint.platform().singleComponent(EventDispatcher.class).publish(new IdleEvent(nettyChannel));
        }
        super.userEventTriggered(ctx, evt);
    }

    private IdleStateHandler newIdleStateHandler() {
        int allIdleTime = endpoint.config().option(EndpointConfig.IDLE_TRIGGER_INTERVAL);
        return new IdleStateHandler(0, 0, allIdleTime, TimeUnit.MILLISECONDS);
    }
}
