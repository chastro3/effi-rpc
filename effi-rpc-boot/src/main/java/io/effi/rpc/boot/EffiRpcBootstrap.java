package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.CollectionUtil;

import java.net.InetSocketAddress;

/**
 * Bootstrap class for initializing and configuring EffiRpc framework.
 */
public class EffiRpcBootstrap extends ScopedApplication.Holder {

    private static final Logger logger = LoggerFactory.getLogger(EffiRpcBootstrap.class);

    EffiRpcBootstrap(ScopedApplication application) {
        super(application);
    }

    public static EffiRpcBootstrap newInstance(ScopedPlatform platform, String applicationName) {
        return newInstance(platform.newApplication(applicationName));
    }

    /**
     * Creates a new instance of EffiRpcBootstrap with the specified application name.
     *
     * @param applicationName the name of the application
     * @return a new instance of EffiRpcBootstrap
     */
    public static EffiRpcBootstrap newInstance(String applicationName) {
        return newInstance(ScopedPlatform.defaultPlatform(), applicationName);
    }

    /**
     * Creates a new instance of EffiRpcBootstrap with the given application.
     *
     * @param application the EffiRpcApplication instance
     * @return a new instance of EffiRpcBootstrap
     */
    public static EffiRpcBootstrap newInstance(ScopedApplication application) {
        return new EffiRpcBootstrap(application);
    }

    public EffiRpcBootstrap applyServer(ServerConfig serverConfig, int port) {
        ServerLauncher.allocate(application, serverConfig, port);
        return this;
    }

    public EffiRpcBootstrap applyServer(ServerConfig serverConfig, String host, int port) {
        ServerLauncher.allocate(application, serverConfig, host, port);
        return this;
    }

    public EffiRpcBootstrap applyServer(ServerConfig serverConfig, InetSocketAddress boundAddress) {
        ServerLauncher.allocate(application, serverConfig, boundAddress);
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
        application().platform()
                .registry()
                .register(RegistryConfig.class, registryConfig);
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
        application.close();
        return this;
    }

    /**
     * Returns the application.
     */
    public ScopedApplication application() {
        return application;
    }

    /**
     * Returns the defaultModule.
     */
    public ScopedModule defaultModule() {
        return application.defaultModule();
    }

}