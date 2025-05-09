package io.effi.rpc.registry;

import io.effi.rpc.util.resoruce.Cleanable;
import io.effi.rpc.spi.Extensible;
import io.effi.rpc.config.URL;
import io.effi.rpc.contract.module.EffRpcApplication;

import static io.effi.rpc.constant.Component.Registry.CONSUL;

/**
 * Creates and retrieves {@link RegistryService} instances.
 */
@Extensible(CONSUL)
public interface RegistryFactory extends Cleanable {

    /**
     * Retrieves a {@link RegistryService} instance for the given configuration URL.

     * @param application the application
     * @param url         the registry service configuration URL
     * @return the {@link RegistryService} instance
     */

    RegistryService getService(EffRpcApplication application, URL url);
}



