package io.effi.rpc.boot;

import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.CollectionUtil;

/**
 * Bootstrap class for initializing and configuring EffiRpc framework.
 */
public class EffiRpcBootstrap extends EffiRpcApplication.Holder {

    private static final Logger logger = LoggerFactory.getLogger(EffiRpcBootstrap.class);


    EffiRpcBootstrap(EffiRpcApplication application) {
        super(application);
    }

    public static EffiRpcBootstrap newInstance(EffiRpcPlatform platform, String applicationName) {
        return newInstance(platform.newApplication(applicationName));
    }

    /**
     * Creates a new instance of EffiRpcBootstrap with the specified application name.
     *
     * @param applicationName the name of the application
     * @return a new instance of EffiRpcBootstrap
     */
    public static EffiRpcBootstrap newInstance(String applicationName) {
        return newInstance(EffiRpcPlatform.getInstance(), applicationName);
    }

    /**
     * Creates a new instance of EffiRpcBootstrap with the given application.
     *
     * @param application the EffiRpcApplication instance
     * @return a new instance of EffiRpcBootstrap
     */
    public static EffiRpcBootstrap newInstance(EffiRpcApplication application) {
        return new EffiRpcBootstrap(application);
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
    public EffiRpcBootstrap serviceHost(ServerConfig serverConfig, int port, RegistryConfig... registryConfigs) {
        EffiRpcPlatform platform = platform();
        DefaultServiceHost serviceHost = DefaultServiceHost.builder()
                .exportedPort(port)
                .serverConfig(serverConfig)
                .registryAt(registryConfigs)
                .platform(platform)
                .build();
        platform.register(ServiceHost.class, serviceHost);
        return this;
    }

    /**
     * Registers multiple services.
     *
     * @param services
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
        AnnotationRemoteService<Object> remoteService = new AnnotationRemoteService<>(service, application);
        return this;
    }

    /**
     * Registers the RPC service with the given registry configuration.
     *
     * @param registryConfig the registry configuration
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap registry(RegistryConfig registryConfig) {
        application().register(RegistryConfig.class, registryConfig);
        return this;
    }

    /**
     * Starts the EffiRpc application.
     */
    public EffiRpcBootstrap start() {
        application.start();
        return this;
    }

    /**
     * Stops the EffiRpc application.
     */
    public EffiRpcBootstrap stop() {
        application.stop();
        return this;
    }

    /**
     * Returns the application.
     */
    public EffiRpcApplication application() {
        return application;
    }

    /**
     * Returns the defaultModule.
     */
    public EffiRpcModule defaultModule() {
        return application.defaultModule();
    }

}