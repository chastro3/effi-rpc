package io.effi.rpc.transport.server;

import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.NetUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Manage active Channel connections for {@link Server}.
 */
@Sharable
public class ChannelManageHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "channelManageHandler";

    private final Map<String, Channel> activeChannels;

    public ChannelManageHandler(Map<String, Channel> activeChannels) {
        this.activeChannels = AssertUtil.notNull(activeChannels, "activeChannels");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String remoteAddress = NetUtil.toAddress((InetSocketAddress) channel.remoteAddress());
        activeChannels.put(remoteAddress, channel);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddress = NetUtil.toAddress((InetSocketAddress) ctx.channel().remoteAddress());
        activeChannels.remove(remoteAddress);
        super.channelInactive(ctx);
    }

}

