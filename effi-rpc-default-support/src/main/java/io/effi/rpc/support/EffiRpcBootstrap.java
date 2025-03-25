package io.effi.rpc.support;

import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Bootstrap class for initializing and configuring EffiRpc framework.
 */
public class EffiRpcBootstrap {

    private final EffRpcApplication application;

    EffiRpcBootstrap(EffRpcApplication application) {
        this.application = AssertUtil.notNull(application, "application");
    }

    /**
     * Creates a new instance of EffiRpcBootstrap with the specified application name.
     *
     * @param applicationName the name of the application
     * @return a new instance of EffiRpcBootstrap
     */
    public static EffiRpcBootstrap newInstance(String applicationName) {
        return new EffiRpcBootstrap(new EffRpcApplication(applicationName));
    }

    /**
     * Creates a new instance of EffiRpcBootstrap with the given application.
     *
     * @param application the EffRpcApplication instance
     * @return a new instance of EffiRpcBootstrap
     */
    public static EffiRpcBootstrap newInstance(EffRpcApplication application) {
        return new EffiRpcBootstrap(application);
    }

    /**
     * Exports a service with the given server configuration and port.
     *
     * @param serverConfig   the server configuration
     * @param port           the port to export on
     * @param registryConfig optional registry configurations
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap exported(ServerConfig serverConfig, int port, RegistryConfig... registryConfig) {
        return exported(serverConfig, port, defaultModule(), registryConfig);
    }

    /**
     * Exports a service using a specific module.
     *
     * @param serverConfig    the server configuration
     * @param port            the port to export on
     * @param module          the EffiRpc module
     * @param registryConfigs optional registry configurations
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap exported(ServerConfig serverConfig, int port, EffiRpcModule module, RegistryConfig... registryConfigs) {
        if (module.application() == application) {
            DefaultServerExporter serverExporter = DefaultServerExporter.builder()
                    .exportedPort(port)
                    .serverConfig(serverConfig)
                    .module(module)
                    .build();
            serverExporter.registry(registryConfigs);
        }
        return this;
    }

    /**
     * Registers multiple services.
     *
     * @param services
     * @return
     */
    public EffiRpcBootstrap services(Object... services) {
        if (CollectionUtil.isNotEmpty(services)) {
            for (Object service : services) {
                service(service);
            }
        }
        return this;
    }

    /**
     * Registers a service.
     *
     * @param service the service instance
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap service(Object service) {
        new AnnotationRemoteService<>(service, application);
        return this;
    }

    /**
     * Registers the RPC service with the given registry configuration.
     *
     * @param registryConfig the registry configuration
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap registry(RegistryConfig registryConfig) {
        return registry(defaultModule(), registryConfig);
    }

    /**
     * Registers the RPC service with a specific module and registry configuration.
     *
     * @param module         the EffiRpc module
     * @param registryConfig the registry configuration
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap registry(EffiRpcModule module, RegistryConfig registryConfig) {
        if (module != null) {
            module.registerShared(registryConfig);
        }
        return this;
    }

    /**
     * Starts the EffiRpc application.
     *
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap start() {
        application.start();
        return this;
    }

    /**
     * Stops the EffiRpc application.
     *
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap stop() {
        application.stop();
        return this;
    }

    /**
     * Returns the application.
     *
     * @return the application
     */
    public EffRpcApplication application() {
        return application;
    }

    /**
     * Returns the defaultModule.
     *
     * @return the defaultModule
     */
    public EffiRpcModule defaultModule() {
        return application.defaultModule();
    }

}