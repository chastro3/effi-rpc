package io.effi.rpc.transport.endpoint;

import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.common.exception.PredefinedErrorCode.BIND;

/**
 * Abstract implementation of {@link Server}.
 */
public abstract class AbstractServer extends AbstractEndpoint implements Server {

    protected Map<String, Channel> activeChannels;

    private boolean isInit = false;

    protected AbstractServer(URL url, EffiRpcModule module) {
        super(url, module);
        this.activeChannels = new ConcurrentHashMap<>();
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
        } catch (Throwable e) {
            throw BIND.fail(e, url().address(), url().protocol());
        }
    }

    @Override
    public void close() {
        for (Channel channel : channels()) {
            try {
                channel.close();
            } catch (Throwable e) {
                throw PredefinedErrorCode.CLOSE.fail(e, channel, url.protocol());
            }
        }
        try {
            doClose();
        } catch (Throwable e) {
            throw PredefinedErrorCode.CLOSE.fail(e, this, url.protocol());
        }
    }

    @Override
    public Collection<Channel> channels() {
        return activeChannels.values();
    }

    @Override
    public Channel acquireChannel(InetSocketAddress remoteAddress) {
        for (Channel channel : channels()) {
            if (NetUtil.isSameAddress(channel.remoteAddress(), remoteAddress)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("url=%s, active=%s", url(), isActive());
    }

    protected abstract void doInit();

    protected abstract void doBind() throws Throwable;

    protected abstract void doClose() throws Throwable;

}
