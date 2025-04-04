package io.effi.rpc.transport.netty;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Base on {@link IdleStateHandler}.
 */
@Sharable
public class NettyIdleStateHandler extends IdleStateHandler {

    private final NettyHeartBeatHandler heartBeatHandler;

    public NettyIdleStateHandler(URL endpointUrl, EffiRpcModule module) {
        super(0, 0, getHeartbeatInterval(endpointUrl), TimeUnit.MILLISECONDS);
        this.heartBeatHandler = new NettyHeartBeatHandler(endpointUrl, module);
    }

    private static int getHeartbeatInterval(URL url) {
        return url.getIntParam(KeyConstant.HEART_BEAT_INTERVAL, Constant.DEFAULT_HEART_BEAT_INTERVAL);
    }

    public NettyHeartBeatHandler heartBeatHandler() {
        return heartBeatHandler;
    }
}

