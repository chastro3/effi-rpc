package io.effi.rpc.transport.netty.tcp;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.transport.netty.NettyEndpointConfig;
import io.effi.rpc.transport.netty.NettyIdleStateHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;

/**
 * Initializes the channel of Netty for Custom Codec.
 */
public class TcpClientChannelPoolHandler extends AbstractChannelPoolHandler {

    private final NettyEndpointConfig config;

    public TcpClientChannelPoolHandler(NettyEndpointConfig config) {
        this.config = config;
    }

    @Override
    public void channelCreated(Channel channel) throws Exception {
        URL url = config.url();
        NettyIdleStateHandler idleStateHandler = new NettyIdleStateHandler(url, config.module());
        ChannelPipeline pipeline = channel.pipeline();
        TcpCodec tcpCodec = new TcpCodec(url, true);
        pipeline.addLast("decoder", tcpCodec.encoder())
                .addLast("encoder", tcpCodec.decoder())
                .addLast("idleState", idleStateHandler)
                .addLast("heartbeat", idleStateHandler.heartBeatHandler());
    }
}
