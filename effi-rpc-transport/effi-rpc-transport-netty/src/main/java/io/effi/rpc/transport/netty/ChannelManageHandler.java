package io.effi.rpc.transport.netty;

import io.effi.rpc.common.config.URL;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.endpoint.Server;
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

    private final URL serverUrl;

    private final EffiRpcModule module;

    private final Map<String, io.effi.rpc.transport.endpoint.Channel> activeChannels;

    public ChannelManageHandler(Map<String, io.effi.rpc.transport.endpoint.Channel> activeChannels, Server server) {
        this.activeChannels = AssertUtil.notNull(activeChannels, "activeChannels");
        AssertUtil.notNull(server, "server");
        this.serverUrl = server.url();
        this.module = server.module();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String remoteAddress = NetUtil.toAddress((InetSocketAddress) channel.remoteAddress());
        activeChannels.put(remoteAddress, NettyChannel.get(channel));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddress = NetUtil.toAddress((InetSocketAddress) ctx.channel().remoteAddress());
        activeChannels.remove(remoteAddress);
        super.channelInactive(ctx);
    }

}

