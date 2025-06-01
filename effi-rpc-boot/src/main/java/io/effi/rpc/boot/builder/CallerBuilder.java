package io.effi.rpc.boot.builder;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.RemoteClient;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.TypeToken;

import java.net.InetSocketAddress;

/**
 * Builds {@link Caller} instance and defines configuration.
 */
public abstract class CallerBuilder<T extends Caller<?>, C extends CallerBuilder<T, C>>
        extends InvokerBuilder<T, C> {

    protected Locator locator;

    protected ClientConfig clientConfig;

    protected CallerBuilder(TypeToken<?> returnType, NodeConfig config) {
        super(config);
        this.returnType = AssertUtil.notNull(returnType, "returnType");
    }

    /**
     * Sets direct address.
     */
    public C directAddress(String address) {
        config.set(DefaultConfigKeys.ADDRESS.key(), address);
        return returnThis();
    }

    /**
     * Sets direct address.
     */
    public C directAddress(InetSocketAddress address) {
        return directAddress(NetUtil.toAddress(address));
    }

    /**
     * Sets remote application.
     */
    public C remoteApplication(String applicationName) {
        config.set(DefaultConfigKeys.REMOTE_APPLICATION, applicationName);
        return returnThis();
    }

    /**
     * Sets remote module.
     */
    public C remoteModule(String moduleName) {
        config.set(DefaultConfigKeys.REMOTE_MODULE, moduleName);
        return returnThis();
    }

    /**
     * Sets container.
     */
    public C container(RemoteClient<?> client) {
        this.container = client;
        return returnThis();
    }

    /**
     * Sets service locator.
     */
    public C locator(Locator locator) {
        this.locator = locator;
        return returnThis();
    }

    /**
     * Sets client configuration.
     */
    public C clientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return returnThis();
    }

    /**
     * Sets retry attempts.
     */
    public C retries(int retries) {
        config.set(DefaultConfigKeys.RETRIES, String.valueOf(retries));
        return returnThis();
    }

    /**
     * Sets load balancing strategy.
     */
    public C loadBalance(String loadBalance) {
        config.set(DefaultConfigKeys.LOAD_BALANCE, loadBalance);
        return returnThis();
    }

    /**
     * Sets fault tolerance strategy.
     */
    public C faultTolerance(String faultTolerance) {
        config.set(DefaultConfigKeys.FAULT_TOLERANCE, faultTolerance);
        return returnThis();
    }

    /**
     * Sets call timeout.
     */
    public C timeout(int timeout) {
        config.set(DefaultConfigKeys.TIMEOUT, String.valueOf(timeout));
        return returnThis();
    }

    /**
     * Returns the locator.
     */
    public Locator locator() {
        return locator;
    }

    /**
     * Returns the client config.
     */
    public ClientConfig clientConfig() {
        return clientConfig;
    }
}


