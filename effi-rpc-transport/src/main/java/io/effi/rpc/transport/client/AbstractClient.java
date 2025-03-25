package io.effi.rpc.transport.client;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.endpoint.AbstractEndpoint;

import java.net.ConnectException;

import static io.effi.rpc.common.util.ObjectUtil.simpleClassName;

/**
 * Abstract implementation of {@link Client}.
 */
public abstract class AbstractClient extends AbstractEndpoint implements Client {

    protected int connectTimeout;

    protected boolean isInit = false;

    protected AbstractClient(InitializedConfig config) {
        super(config);
        this.connectTimeout = url().getIntParam(DefaultConfigKeys.CONNECT_TIMEOUT.key(), Constant.DEFAULT_CONNECT_TIMEOUT);
        try {
            connect();
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.debug("Created {},connect to remoteAddress: {}", simpleClassName(this), address());
        } catch (ConnectException e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.CONNECT, e, url().address(), url().protocol());
        }
    }

    @Override
    public void connect() throws ConnectException {
        if (isActive()) {
            return;
        }
        // When reconnecting, there is no need to initialize again
        if (!isInit) {
            doInit();
            isInit = true;
        }
        doConnect();
    }

    @Override
    public String toString() {
        return simpleClassName(this) + " connect to " + NetUtil.toAddress(inetSocketAddress());
    }

    protected abstract void doInit();

    protected abstract void doConnect() throws ConnectException;

}
