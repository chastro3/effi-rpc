package io.effi.rpc.transport.tcp;

import io.effi.rpc.common.constant.HandlerNames;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.NettyIdleStateHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import static io.effi.rpc.transport.NettyIdleStateHandler.createForClient;
import static io.effi.rpc.transport.NettyIdleStateHandler.createForServer;

/**
 * Initializes the channel of Netty for Custom Codec.
 */
public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final InitializedConfig config;

    private final boolean isServer;

    public TcpChannelInitializer(InitializedConfig config, boolean isServer) {
        this.config = config;
        this.isServer = isServer;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        URL url = config.url();
        EffiRpcModule module = config.module();
        NettyIdleStateHandler idleStateHandler = isServer ? createForServer(url, module) : createForClient(url, module);
        ChannelPipeline pipeline = channel.pipeline();
        TcpCodec tcpCodec = new TcpCodec(url, true);
        pipeline.addLast(HandlerNames.IDLE_STATE, idleStateHandler)
                .addLast(HandlerNames.HEARTBEAT, idleStateHandler.handler())
                .addLast("decoder", tcpCodec.decoder())
                .addLast("encoder", tcpCodec.encoder());
//                .addLast(HandlerNames.ADAPTER, config.handler());

    }
}
