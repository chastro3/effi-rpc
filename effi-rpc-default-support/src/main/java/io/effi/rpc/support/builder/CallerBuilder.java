package io.effi.rpc.support.builder;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.extension.TypeToken;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.module.EffiRpcModule;

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

    /**
     * Constructs a builder with a specified return type.
     *
     * @param returnType
     */
    protected CallerBuilder(TypeToken<?> returnType, Config config) {
        super(config);
        this.returnType = returnType;
    }

    /**
     * Sets the communication module.
     *
     * @param module module instance to use
     * @return This builder
     */
    public C module(EffiRpcModule module) {
        this.module = module;
        return returnThis();
    }

    /**
     * Sets service locator.
     *
     * @param locator Locator instance to use
     * @return This builder
     */
    public C locator(Locator locator) {
        this.locator = locator;
        return returnThis();
    }

    /**
     * Sets client configuration.
     *
     * @param clientConfig Client configuration to apply
     * @return This builder
     */
    public C clientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return returnThis();
    }

    /**
     * Sets retry attempts.
     *
     * @param retries Number of retries
     * @return This builder
     */
    public C retries(int retries) {
        config.set(DefaultConfigKeys.RETRIES.key(), String.valueOf(retries));
        return returnThis();
    }

    /**
     * Sets load balancing strategy.
     *
     * @param loadBalance Load balancing strategy
     * @return This builder
     */
    public C loadBalance(String loadBalance) {
        config.set(DefaultConfigKeys.LOAD_BALANCE.key(), loadBalance);
        return returnThis();
    }

    /**
     * Sets fault tolerance strategy.
     *
     * @param faultTolerance Fault tolerance method
     * @return This builder
     */
    public C faultTolerance(String faultTolerance) {
        config.set(DefaultConfigKeys.FAULT_TOLERANCE.key(), faultTolerance);
        return returnThis();
    }

    /**
     * Sets call timeout.
     *
     * @param timeout Timeout in milliseconds
     * @return This builder
     */
    public C timeout(int timeout) {
        config.set(DefaultConfigKeys.TIMEOUT.key(), String.valueOf(timeout));
        return returnThis();
    }

    /**
     * Returns the locator.
     *
     * @return the locator
     */
    public Locator locator() {
        return locator;
    }

    /**
     * Returns the module.
     *
     * @return the module
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
        return super.build();
    }

    protected abstract ClientConfig defaultConfig();

    private void checkClientConfig() {
        if (clientConfig == null && module != null) {
            String name = config.get(DefaultConfigKeys.CLIENT_CONFIG);
            if (StringUtil.isNotBlank(name)) {
                clientConfig = module.clientConfigManager().get(name);
            }
            if (clientConfig == null) {
                clientConfig = defaultConfig();
                if (clientConfig != null) {
                    module.clientConfigManager().register(clientConfig);
                    config.set(DefaultConfigKeys.CLIENT_CONFIG.key(), clientConfig.name());
                }
            }
        }
    }
}


