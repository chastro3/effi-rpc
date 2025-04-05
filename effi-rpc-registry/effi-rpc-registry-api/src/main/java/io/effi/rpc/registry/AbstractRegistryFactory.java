package io.effi.rpc.registry;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.module.EffRpcApplication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of {@link RegistryFactory}.
 */
public abstract class AbstractRegistryFactory implements RegistryFactory {

    private final Map<String, RegistryService> registryServices = new ConcurrentHashMap<>();

    @Override
    public RegistryService getService(EffRpcApplication application, URL url) {
        String key = application.name() + "-" + url.getParam(KeyConstant.NAME, url.authority());
        // Get or create the RegistryService associated with the given key
        RegistryService registryService = registryServices.computeIfAbsent(key, k -> create(application, url));
        // Connect if the service is not active
        if (!registryService.isActive()) {
            synchronized (key) {
                if (!registryService.isActive()) {
                    registryService.connect(url);
                }
            }
        }
        return registryService;
    }

    @Override
    public void clear() {
        // Close all registered services and clear the map
        registryServices.values().forEach(RegistryService::close);
        registryServices.clear();
    }

    protected abstract RegistryService create(EffRpcApplication application, URL url);
}

