package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;

/**
 * Provides an abstract implementation of {@link Server}.
 */
public abstract class AbstractServer extends AbstractEndpoint implements Server {

    protected AbstractServer(ServerConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform);
        initialize();
    }

    @Override
    public void close() {
        for (Channel channel : channels()) {
            try {
                channel.close();
            } catch (Throwable e) {
                throw PredefinedErrorCode.CLOSE_CHANNEL.fail(e, channel.remoteAddress());
            }
        }
        try {
            doClose();
        } catch (Throwable e) {
            throw PredefinedErrorCode.CLOSE_SERVER.fail(e, url.authority());
        }
    }

    @Override
    public Channel lookupChannel(InetSocketAddress remoteAddress) {
        for (Channel channel : channels()) {
            if (NetUtil.isSameAddress(channel.remoteAddress(), remoteAddress)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public ServerConfig config() {
        return (ServerConfig) config;
    }

    @Override
    public String toString() {
        return StringUtil.format("config={}, active={}", url(), isActive());
    }

    protected abstract void initialize();

    protected abstract void doClose() throws Throwable;

}
