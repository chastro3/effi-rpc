package io.effi.rpc.transport.netty.tcp;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.netty.HandlerNames;
import io.effi.rpc.transport.netty.NettyEndpointConfig;
import io.effi.rpc.transport.netty.NettyIdleStateHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Initializes the channel of Netty for Custom Codec.
 */
public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyEndpointConfig config;

    private final boolean isServer;

    public TcpChannelInitializer(NettyEndpointConfig config, boolean isServer) {
        this.config = config;
        this.isServer = isServer;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        URL url = config.url();
        EffiRpcModule module = config.module();
        NettyIdleStateHandler idleStateHandler = new NettyIdleStateHandler(url, module);
        ChannelPipeline pipeline = channel.pipeline();
        TcpCodec tcpCodec = new TcpCodec(url, true);
        pipeline.addLast(HandlerNames.IDLE_STATE, idleStateHandler)
                .addLast(HandlerNames.HEARTBEAT, idleStateHandler.heartBeatHandler())
                .addLast("decoder", tcpCodec.decoder())
                .addLast("encoder", tcpCodec.encoder());
//                .addLast(HandlerNames.ADAPTER, config.handler());

    }
}
