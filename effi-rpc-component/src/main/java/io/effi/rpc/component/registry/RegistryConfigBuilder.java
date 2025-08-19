package io.effi.rpc.component.registry;

import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.IdentifiableConfigBuilder;
import io.effi.rpc.config.SmartURL;

/**
 * Builds {@link RegistryConfig} instance and defines configuration.
 */
public abstract class RegistryConfigBuilder<T extends RegistryConfig, SELF extends RegistryConfigBuilder<T, SELF>>
        extends IdentifiableConfigBuilder<T, SELF> {

    protected String type;

    protected String address;

    protected ThreadPool threadPool;

    /**
     * Sets the registry type (eg: 'consul','nacos').
     */
    public SELF type(String type) {
        this.type = type;
        return self();
    }

    /**
     * Sets the registry address or addresses.
     * If multiple addresses are specified, they must be separated by ','.
     */
    public SELF address(String address) {
        this.address = address;
        return self();
    }

    /**
     * Sets the registry authority from a URL string.
     * (eg: 'consul://127.0.0.1:8500')
     */
    public SELF authority(String authority) {
        SmartURL smartUrl = SmartURL.valueOf(authority);
        type(smartUrl.scheme());
        address(smartUrl.address());
        return self();
    }

    /**
     * Sets the thread pool for registry operations.
     */
    public SELF threadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return self();
    }

    /**
     * Sets the thread pool for registry operations.
     * @param connectTimeout the connection timeout in milliseconds
     */
    public SELF connectTimeout(int connectTimeout) {
        setConfig(ConfigNames.CONNECT_TIMEOUT, connectTimeout);
        return self();
    }

    /**
     * Sets the number of retry attempts.
     */
    public SELF retries(int retries) {
        setConfig(ConfigNames.RETRIES, retries);
        return self();
    }

    /**
     * Sets the heartbeat interval configuration.
     */
    public SELF heartbeatInterval(int heartbeatInterval) {
        setConfig(ConfigNames.HEARTBEAT_INTERVAL, heartbeatInterval);
        return self();
    }
}


