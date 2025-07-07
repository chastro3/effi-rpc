package io.effi.rpc.transport.netty;

import io.effi.rpc.base.event.EventDispatcher;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.effi.rpc.transport.heartbeat.IdleEvent;
import io.effi.rpc.transport.heartbeat.RefreshIdleCountEvent;
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

import static io.effi.rpc.config.DefaultConfigNames.IDLE_TRIGGER_INTERVAL;
import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Handles heartbeat-related events in Netty channels.
 * <p>Increments idle counters and publishes {@link IdleEvent} and {@link RefreshIdleCountEvent} accordingly.</p>
 */
@Sharable
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private final Endpoint endpoint;

    public HeartBeatHandler(Endpoint endpoint) {
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
        super.channelRead(ctx, msg);
        NettyChannel channel = NettyChannel.get(ctx.channel());
        if (channel != null) {
            endpoint.platform().lookup(EventDispatcher.class)
                    .publish(new RefreshIdleCountEvent(channel));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        NettyChannel nettyChannel = NettyChannel.get(ctx.channel());
        if (nettyChannel != null) {
            if (evt instanceof IdleStateEvent event && event.state() == IdleState.ALL_IDLE) {
                Optional.ofNullable(nettyChannel.get(KeyConstant.IDLE_COUNT))
                        .ifPresent(AtomicInteger::incrementAndGet);
                endpoint.platform().lookup(EventDispatcher.class)
                        .publish(new IdleEvent(nettyChannel));
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    private IdleStateHandler newIdleStateHandler() {
        int allIdleTime = endpoint.url().getIntParam(IDLE_TRIGGER_INTERVAL);
        return new IdleStateHandler(0, 0, allIdleTime, TimeUnit.MILLISECONDS);
    }
}
