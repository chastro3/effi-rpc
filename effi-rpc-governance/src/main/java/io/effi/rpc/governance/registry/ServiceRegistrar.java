package io.effi.rpc.governance.registry;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.List;

public interface ServiceRegistrar extends ScopedApplication.Supplier, Closeable {

    List<ServiceInstance> serviceInstances();

    List<RegistryConfig> registryConfigs();

    void register();

    void deregister();

    @Override
    default void close() {
        deregister();
    }
}
