package io.effi.rpc.transport.netty;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static io.effi.rpc.config.DefaultConfigKeys.IDLE_TRIGGER_INTERVAL;
import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Base on {@link IdleStateHandler}.
 */
@Sharable
public class NettyIdleStateHandler extends IdleStateHandler {

    private final NettyHeartBeatHandler heartBeatHandler;

    public NettyIdleStateHandler(URL endpointUrl, EffiRpcModule module) {
        super(0, 0, endpointUrl.getIntParam(IDLE_TRIGGER_INTERVAL), TimeUnit.MILLISECONDS);
        this.heartBeatHandler = new NettyHeartBeatHandler(endpointUrl, module);
    }

    public NettyHeartBeatHandler heartBeatHandler() {
        return heartBeatHandler;
    }
}

