package io.effi.rpc.spring.consumer;

import io.effi.rpc.boot.InterfaceCallerGroup;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.spring.autoconfigure.EffiRpcProperties;

import java.util.function.Consumer;

/**
 * Provides creation of interface-based consumer proxies backed by Spring configuration.
 */
public final class InterfaceCallGroupFactory {

    private final ScopedModule module;

    private final EffiRpcProperties properties;

    public InterfaceCallGroupFactory(ScopedModule module, EffiRpcProperties properties) {
        this.module = module;
        this.properties = properties;
    }

    /**
     * Creates a consumer proxy targeting the named remote service.
     *
     * @param targetType the remote interface
     * @param remoteServiceName the remote service name used as the call endpoint
     * @return the consumer proxy
     */
    public <T> T create(Class<T> targetType, String remoteServiceName) {
        return create(targetType, options -> options.addOption(CallerOptions.ENDPOINT, remoteServiceName));
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
        ConsumerOptionMapper.apply(options, properties.consumer());
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
