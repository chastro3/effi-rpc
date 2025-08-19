package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.async.Future;
import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryClientFactory;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.effi.rpc.config.ConfigValues.DEFAULT;

/**
 * Provides the default implementation of {@link ServiceDiscovery}.
 * <p>Deduplication based on address.</p>
 */
@Extension(DEFAULT)
public class DefaultServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceDiscovery.class);

    @Override
    public List<ServiceInstance> discover(String serviceName, CallContext<Request, Caller<?>> context, Collection<RegistryConfig> registryConfigs) {
        List<ServiceInstance> availableInstances = new ArrayList<>();
        SmartURL smartUrl = context.message().url();
        int size = registryConfigs.size();
        ScopedPlatform platform = context.module().platform();
        List<Future<List<ServiceInstance>>> futures = new ArrayList<>(size);
        for (RegistryConfig registryConfig : registryConfigs) {
            var registryService = platform.namedExtension(RegistryClientFactory.class, registryConfig.type())
                    .fetch(registryConfig);
            futures.add(registryService.lookup(serviceName));
        }
        Promise<Void> promise = Promise.allOf(futures);
        promise.await();
        for (Future<List<ServiceInstance>> future : futures) {
            List<ServiceInstance> discoveredInstances = future.result();
            if (CollectionUtil.isNotEmpty(discoveredInstances)) {
                for (ServiceInstance discoveredInstance : discoveredInstances) {
                    if (discoveredInstance.protocol().equals(smartUrl.scheme())) {
                        CollectionUtil.addToList(
                                availableInstances,
                                (existedInst, newInst) -> Objects.equals(existedInst.id(), newInst.id()),
                                discoveredInstance);
                    }
                }
            }
        }
        if (availableInstances.isEmpty()) {
            throw PredefinedErrorCode.NOT_FOUND_SERVICE.fail(null, serviceName);
        }
        return availableInstances;
    }
}
