package io.effi.rpc.governance.registry;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.trait.Closeable;

import java.util.Collection;

public interface ServiceRegistrar extends ScopedApplication.Supplier, Closeable {

    Collection<ServiceInstance> serviceInstances();

    Collection<RegistryConfig> registryConfigs();

    Future<Void> register();

    Future<Void> deregister();

    @Override
    default void close() {
        deregister();
    }
}
