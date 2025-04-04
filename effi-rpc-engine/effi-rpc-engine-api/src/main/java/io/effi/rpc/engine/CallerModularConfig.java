package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.filter.*;
import io.effi.rpc.contract.manager.RegistryConfigManager;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures caller-related modular components.
 */
public class CallerModularConfig extends InvokerModularConfig<Caller<?>> {

    private final List<RegistryConfig> registryConfigs = new ArrayList<>();

    private final List<ChosenFilter<?, ?>> chosenFilters = new ArrayList<>();

    private ClientConfig clientConfig;

    public CallerModularConfig(EffiRpcModule module, ClientConfig clientConfig, Caller<?> caller) {
        super(module, caller);
        this.clientConfig = clientConfig;
        addConfiguredFilters();
        addConfiguredClientConfig();
        addConfiguredRegistryConfigs();
    }

    @Override
    public void addFilter(Filter<?, ?, ?>... filters) {
        if (CollectionUtil.isNotEmpty(filters)) {
            Class<? extends Envelope.Request> supportedRequestType = getSupportedRequestType(invoker);
            Class<? extends Envelope.Response> supportedResponseType = getSupportedResponseType(invoker);
            for (Filter<?, ?, ?> filter : filters) {
                FilterType<?, ?> type = FilterSupport.getType(filter);
                Class<? extends Envelope> envelopeType = type.envelopeType();
                if (type.invokerType().isAssignableFrom(invoker.getClass())) {
                    switch (filter) {
                        case InvokeFilter<?, ?> invokeFilter -> {
                            if (envelopeType.isAssignableFrom(supportedRequestType)) {
                                CollectionUtil.addUnique(invokeFilters, invokeFilter);
                            }
                        }
                        case ChosenFilter<?, ?> chosenFilter -> {
                            if (envelopeType.isAssignableFrom(supportedRequestType)) {
                                CollectionUtil.addUnique(chosenFilters, chosenFilter);
                            }
                        }
                        case ReplyFilter<?, ?> replyFilter -> {
                            if (envelopeType.isAssignableFrom(supportedResponseType)) {
                                CollectionUtil.addUnique(replyFilters, replyFilter);
                            }
                        }
                        default -> throw new IllegalArgumentException(Messages.unSupport("filter", filter.getClass()));
                    }
                }
            }
        }
    }

    /**
     * Returns the chosenFilters.
     */
    public List<ChosenFilter<?, ?>> chosenFilters() {
        return chosenFilters;
    }

    /**
     * Returns the registryConfigs.
     */
    public List<RegistryConfig> registryConfigs() {
        return registryConfigs;
    }

    /**
     * Returns the clientConfig.
     */
    public ClientConfig clientConfig() {
        return clientConfig;
    }

    private void addConfiguredClientConfig() {
        String clientConfigName = invoker.get(DefaultConfigKeys.CLIENT_CONFIG);
        clientConfig = module.clientConfigManager().get(clientConfigName);
    }

    private void addConfiguredRegistryConfigs() {
        List<String> registryConfigNames = invoker.getMerged(DefaultConfigKeys.REGISTRIES);
        RegistryConfigManager registryConfigManager = module.registryConfigManager();
        for (RegistryConfig registryConfig : registryConfigManager.sharedValues()) {
            CollectionUtil.addUnique(registryConfigs, registryConfig);
        }
        for (String registryConfigName : registryConfigNames) {
            RegistryConfig registryConfig = registryConfigManager.get(registryConfigName);
            if (registryConfig != null) CollectionUtil.addUnique(registryConfigs, registryConfig);
        }
    }

}
