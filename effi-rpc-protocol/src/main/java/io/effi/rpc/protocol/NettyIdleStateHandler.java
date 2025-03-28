package io.effi.rpc.protocol;

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

    private final NettyHeartBeatHandler handler;

    protected NettyIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds,
                                    int allIdleTimeSeconds, NettyHeartBeatHandler handler) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.MILLISECONDS);
        this.handler = handler;
    }

    /**
     * Create a {@link IdleStateHandler} for server.
     *
     * @param url
     * @param module
     * @return
     */
    public static NettyIdleStateHandler createForServer(URL url, EffiRpcModule module) {
        return createForServer(getKeeAliveTimeout(url), new NettyHeartBeatHandler(url, module, true));
    }

    /**
     * Create a {@link IdleStateHandler} for client.
     *
     * @param url
     * @param module
     * @return
     */
    public static NettyIdleStateHandler createForClient(URL url, EffiRpcModule module) {
        return createForClient(getKeeAliveTimeout(url), new NettyHeartBeatHandler(url, module, false));
    }

    protected static NettyIdleStateHandler createForServer(int heartbeatInterval, NettyHeartBeatHandler handler) {
        return new NettyIdleStateHandler(heartbeatInterval, 0, heartbeatInterval, handler);
    }

    protected static NettyIdleStateHandler createForClient(int heartbeatInterval, NettyHeartBeatHandler handler) {
        return new NettyIdleStateHandler(0, heartbeatInterval, heartbeatInterval, handler);
    }

    private static int getKeeAliveTimeout(URL url) {
        return url.getIntParam(KeyConstant.KEEP_ALIVE_TIMEOUT, Constant.DEFAULT_KEEP_ALIVE_TIMEOUT);
    }

    /**
     * Returns the handler.
     *
     * @return the handler
     */
    public NettyHeartBeatHandler handler() {
        return handler;
    }
}
