package io.effi.rpc.transport.endpoint;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.contract.module.EffiRpcModule;

import static io.effi.rpc.common.util.ObjectUtil.simpleClassName;

/**
 * Abstract implementation of {@link Client}.
 */
public abstract class AbstractClient extends AbstractEndpoint implements Client {

    protected int connectTimeout;

    protected boolean isInit = false;

    protected AbstractClient(URL url, EffiRpcModule module) {
        super(url, module);
        this.connectTimeout = url().getIntParam(DefaultConfigKeys.CONNECT_TIMEOUT.key(), Constant.DEFAULT_CONNECT_TIMEOUT);
    }

    @Override
    public void connect() {
        if (isActive()) {
            return;
        }
        // When reconnecting, there is no need to initialize again
        if (!isInit) {
            doInit();
            isInit = true;
        }
        try {
            doConnect();
        } catch (Exception e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.CONNECT, e, url().address(), url().protocol());
        }
    }

    @Override
    public String toString() {
        return simpleClassName(this) + " connect to " + NetUtil.toAddress(socketAddress());
    }

    protected abstract void doInit();

    protected abstract void doConnect() throws Exception;

}
