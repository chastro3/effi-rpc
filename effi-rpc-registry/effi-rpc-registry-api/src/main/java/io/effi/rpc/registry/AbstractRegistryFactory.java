package io.effi.rpc.registry;

import io.effi.rpc.config.registry.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an abstract implementation of {@link RegistryFactory}.
 */
public abstract class AbstractRegistryFactory implements RegistryFactory {

    private final Map<String, RegistryService> registryServices = new ConcurrentHashMap<>();

    @Override
    public RegistryService getService(RegistryConfig config) {
        return registryServices.computeIfAbsent(config.id(), k -> newService(config));
    }

    @Override
    public void clear() {
        registryServices.values().forEach(RegistryService::close);
        registryServices.clear();
    }

    protected abstract RegistryService newService(RegistryConfig config);
}

