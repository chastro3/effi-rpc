package io.effi.rpc.transport.netty;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.heartbeat.IdleEvent;
import io.effi.rpc.transport.heartbeat.RefreshIdleCountEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Netty HeartBeatHandler Adapter.
 */
@Sharable
public class NettyHeartBeatHandler extends ChannelInboundHandlerAdapter {

    private final URL endpointUrl;

    private final EffiRpcModule module;

    public NettyHeartBeatHandler(URL endpointUrl, EffiRpcModule module) {
        this.endpointUrl = endpointUrl;
        this.module = module;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel.save(ctx.channel(), endpointUrl, module);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        NettyChannel.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        NettyChannel nettyChannel = NettyChannel.getOrCreate(ctx.channel(), endpointUrl, module);
        module.application().publishEvent(new RefreshIdleCountEvent(nettyChannel));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        NettyChannel nettyChannel = NettyChannel.get(ctx.channel());
        if (evt instanceof IdleStateEvent event && event.state() == IdleState.ALL_IDLE) {
            Optional.ofNullable(nettyChannel.get(KeyConstant.IDLE_COUNT))
                    .ifPresent(AtomicInteger::incrementAndGet);
            module.application().publishEvent(new IdleEvent(nettyChannel));
        }
        super.userEventTriggered(ctx, evt);
    }
}
