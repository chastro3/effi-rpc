package io.effi.rpc.spring.bean;

import io.effi.rpc.boot.InterfaceCallerGroup;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.spring.properties.EffiRpcProperties;

import java.util.function.Consumer;

/**
 * Creates interface-based consumer proxies backed by Spring configuration.
 */
public final class EffiRpcConsumerFactory {

    private final ScopedModule module;

    private final EffiRpcProperties properties;

    public EffiRpcConsumerFactory(ScopedModule module, EffiRpcProperties properties) {
        this.module = module;
        this.properties = properties;
    }

    /**
     * Creates a consumer proxy using the configured caller defaults.
     *
     * @param targetType the remote interface
     * @return the consumer proxy
     */
    public <T> T create(Class<T> targetType) {
        return create(targetType, null);
    }

    /**
     * Creates a consumer proxy using the configured caller defaults and the supplied overrides.
     *
     * @param targetType the remote interface
     * @param customizer caller option overrides
     * @return the consumer proxy
     */
    public <T> T create(Class<T> targetType, Consumer<HierarchicalOptions> customizer) {
        HierarchicalOptions options = HierarchicalOptions.create();
        EffiRpcConsumerOptions.apply(options, properties.consumer());
        if (customizer != null) {
            customizer.accept(options);
        }
        return InterfaceCallerGroup.<T>builder()
                .targetType(targetType)
                .module(module)
                .options(options)
                .build()
                .proxy();
    }
}
