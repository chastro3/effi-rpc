package io.effi.rpc.protocol.tcp;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.protocol.InitializedConfig;
import io.effi.rpc.protocol.NettyIdleStateHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;

import static io.effi.rpc.protocol.NettyIdleStateHandler.createForClient;

/**
 * Initializes the channel of Netty for Custom Codec.
 */
public class TcpClientChannelPoolHandler extends AbstractChannelPoolHandler {

    private final InitializedConfig config;

    public TcpClientChannelPoolHandler(InitializedConfig config) {
        this.config = config;
    }

    @Override
    public void channelCreated(Channel channel) throws Exception {
        URL url = config.url();
        NettyIdleStateHandler idleStateHandler = createForClient(url, config.module());
        ChannelPipeline pipeline = channel.pipeline();
        TcpCodec tcpCodec = new TcpCodec(url, true);
        pipeline.addLast("decoder", tcpCodec.encoder())
                .addLast("encoder", tcpCodec.decoder())
                .addLast("idleState", idleStateHandler)
                .addLast("heartbeat", idleStateHandler.handler());
    }
}
