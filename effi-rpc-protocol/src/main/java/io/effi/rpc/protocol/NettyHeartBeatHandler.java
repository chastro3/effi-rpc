package io.effi.rpc.protocol;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.event.Event;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.protocol.event.IdleEvent;
import io.effi.rpc.protocol.event.RefreshHeartBeatCountEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Netty HeartBeatHandler Adapter.
 */
@Sharable
public class NettyHeartBeatHandler extends ChannelInboundHandlerAdapter {

    private final URL endpointUrl;

    private final EffiRpcModule module;

    private final Boolean isServer;

    public NettyHeartBeatHandler(URL endpointUrl, EffiRpcModule module, boolean isServer) {
        this.endpointUrl = endpointUrl;
        this.module = module;
        this.isServer = isServer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel.save(ctx.channel(), endpointUrl, module);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        NettyChannel nettyChannel = NettyChannel.acquire(ctx.channel(), endpointUrl, module);
        publishRefreshHeartbeatCountEvent(nettyChannel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        NettyChannel.remove(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        NettyChannel nettyChannel = NettyChannel.acquire(ctx.channel());
        if (evt instanceof IdleStateEvent event) {
            IdleState state = event.state();
            AtomicInteger idleTimes = switch (state) {
                case ALL_IDLE -> nettyChannel.get(KeyConstant.ALL_IDLE_TIMES);
                case READER_IDLE -> isServer ? nettyChannel.get(KeyConstant.READER_IDLE_TIMES) : null;
                case WRITER_IDLE -> !isServer ? nettyChannel.get(KeyConstant.WRITE_IDLE_TIMES) : null;
            };
            if (idleTimes != null) {
                idleTimes.incrementAndGet();
            }
            if (state == IdleState.ALL_IDLE
                    || (isServer && state == IdleState.READER_IDLE)
                    || (!isServer && state == IdleState.WRITER_IDLE)) {
                module.application().publishEvent(new IdleEvent(nettyChannel));
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private void publishRefreshHeartbeatCountEvent(NettyChannel channel) {
        Event<?> event = isServer
                ? RefreshHeartBeatCountEvent.buildForServer(channel)
                : RefreshHeartBeatCountEvent.buildForClient(channel);
        module.application().publishEvent(event);
    }
}
