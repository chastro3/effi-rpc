package io.effi.rpc.transport.endpoint;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.module.EffiRpcModule;

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
            throw PredefinedErrorCode.CONNECT.fail(e, url().authority());
        }
    }

    @Override
    public String toString() {
        return String.format("config=%s, active=%b", url(), isActive());
    }

    protected abstract void doInit();

    protected abstract void doConnect() throws Exception;

}
