package io.effi.rpc.registry;

import io.effi.rpc.common.util.resoruce.Cleanable;
import io.effi.rpc.common.spi.Extensible;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.module.EffRpcApplication;

import static io.effi.rpc.common.constant.Component.Registry.CONSUL;

/**
 * Factory for creating and retrieving {@link RegistryService} instances.
 */
@Extensible(CONSUL)
public interface RegistryFactory extends Cleanable {

    /**
     * Returns a {@link RegistryService} instance for the given URL.
     *
     * @param application the application
     * @param url         the registry service configuration URL
     * @return the {@link RegistryService} instance
     */
    RegistryService getService(EffRpcApplication application, URL url);
}



