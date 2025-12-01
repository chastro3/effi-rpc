package io.effi.rpc.registry;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an abstract implementation of {@link RegistryClient.Factory}.
 */
public abstract class AbstractRegistryClientFactory implements RegistryClient.Factory, ScopedPlatform.Acceptor {

    private final Map<String, RegistryClient> registryServices = new ConcurrentHashMap<>();

    protected ScopedPlatform platform;

    @Override
    public void accept(ScopedPlatform platform) {
        this.platform = platform;
    }

    @Override
    public RegistryClient fetch(RegistryConfig config) {
        return registryServices.computeIfAbsent(config.id(), k -> newClient(config, platform));
    }

    @Override
    public void clear() {
        registryServices.values().forEach(RegistryClient::close);
        registryServices.clear();
    }

    protected abstract RegistryClient newClient(RegistryConfig config, ScopedPlatform platform);
}

