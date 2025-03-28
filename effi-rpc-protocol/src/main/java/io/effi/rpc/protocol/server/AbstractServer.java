package io.effi.rpc.protocol.server;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.protocol.InitializedConfig;
import io.effi.rpc.protocol.endpoint.AbstractEndpoint;
import io.netty.channel.Channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of {@link Server}.
 */
public abstract class AbstractServer extends AbstractEndpoint implements Server {

    protected Map<String, Channel> activeChannels;

    private boolean isInit = false;

    protected AbstractServer(InitializedConfig config) {
        super(config);
        this.activeChannels = new ConcurrentHashMap<>();
        bind();
    }

    @Override
    public void bind() {
        if (isActive()) {
            return;
        }
        if (!isInit) {
            doInit();
            isInit = true;
        }
        try {
            doBind();
        } catch (Exception e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.BIND, e, url().address(), url().protocol());
        }
    }

    @Override
    public void close() {
        for (Channel channel : channels()) {
            try {
                channel.close();
            } catch (Throwable e) {
                throw EffiRpcException.wrap(PredefinedErrorCode.CLOSE, e);
            }
        }
        try {
            doClose();
        } catch (IOException e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.CLOSE, e);
        }
    }

    @Override
    public Collection<Channel> channels() {
        return activeChannels.values();
    }

    @Override
    public Channel acquireChannel(InetSocketAddress remoteAddress) {
        for (Channel channel : channels()) {
            if (NetUtil.isSameAddress((InetSocketAddress) channel.remoteAddress(), remoteAddress)) {
                return channel;
            }
        }
        return null;
    }

    protected abstract void doInit();

    protected abstract void doBind() throws Exception;

    protected abstract void doClose() throws IOException;

}
