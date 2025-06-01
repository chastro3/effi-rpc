package io.effi.rpc.transport.netty;

import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks and manages active {@link Channel} connections for a {@link Server}.
 */
@Sharable
public class ChannelManageHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "channelManageHandler";

    private final Map<String, Channel> activeChannels;

    public ChannelManageHandler(Server server) {
        AssertUtil.notNull(server, "server");
        this.activeChannels = new ConcurrentHashMap<>();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel channel = NettyChannel.get(ctx.channel());
        if (channel != null) {
            String remoteAddress = NetUtil.toAddress(channel.remoteAddress());
            activeChannels.put(remoteAddress, channel);
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddress = NetUtil.toAddress((InetSocketAddress) ctx.channel().remoteAddress());
        activeChannels.remove(remoteAddress);
        super.channelInactive(ctx);
    }

    public Collection<Channel> activeChannels() {
        return Collections.unmodifiableCollection(activeChannels.values());
    }

}

