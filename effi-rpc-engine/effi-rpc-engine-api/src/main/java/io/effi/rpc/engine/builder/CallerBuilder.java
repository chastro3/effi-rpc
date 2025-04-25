package io.effi.rpc.engine.builder;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeToken;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.RemoteClient;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.engine.DefaultClientConfig;
import io.effi.rpc.engine.DirectLocator;
import io.effi.rpc.engine.RegistryLocator;

import java.net.InetSocketAddress;

/**
 * Builder for creating {@link Caller} instances,defining settings for caller.
 *
 * @param <T> The type of {@link Caller}.
 * @param <C> The type of this builder.
 */
public abstract class CallerBuilder<T extends Caller<?>, C extends CallerBuilder<T, C>>
        extends InvokerBuilder<T, C> {

    protected Locator locator;

    protected EffiRpcModule module;

    protected ClientConfig clientConfig;

    protected CallerBuilder(TypeToken<?> returnType, NodeConfig config) {
        super(config);
        this.returnType = AssertUtil.notNull(returnType, "returnType");
    }

    /**
     * Sets the module.
     */
    public C module(EffiRpcModule module) {
        this.module = module;
        return returnThis();
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
        config.set(DefaultConfigKeys.APPLICATION, applicationName);
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
     * Returns the module.
     */
    public EffiRpcModule module() {
        return module;
    }

    /**
     * Returns the client config.
     */
    public ClientConfig clientConfig() {
        return clientConfig;
    }

    @Override
    public T build() {
        checkClientConfig();
        checkLocator();
        return super.build();
    }

    protected abstract ClientConfig defaultConfig();

    private void checkClientConfig() {
        if (clientConfig == null && module != null) {
            String name = config.get(DefaultConfigKeys.CLIENT_CONFIG);
            if (StringUtil.isNotBlank(name)) {
                clientConfig = module.clientConfigRepository().get(name);
            }
            if (clientConfig == null) {
                clientConfig = defaultConfig();
                if (clientConfig != null) {
                    module.clientConfigRepository().register(clientConfig);
                    config.set(DefaultConfigKeys.CLIENT_CONFIG.key(), clientConfig.name());
                } else {
                    clientConfig = DefaultClientConfig.builder().protocol(protocol()).build();
                }
            }
        }
    }

    private void checkLocator() {
        if (locator == null) {
            String address = config.get(DefaultConfigKeys.ADDRESS);
            if (StringUtil.isNotBlank(address)) {
                InetSocketAddress socketAddress = NetUtil.toInetSocketAddress(address);
                locator = DirectLocator.getInstance(socketAddress);
            } else {
                String remoteApplication = config.get(DefaultConfigKeys.APPLICATION);
                if (StringUtil.isNotBlank(remoteApplication)) {
                    locator = RegistryLocator.getInstance(remoteApplication);
                }
            }
        }
    }
}


