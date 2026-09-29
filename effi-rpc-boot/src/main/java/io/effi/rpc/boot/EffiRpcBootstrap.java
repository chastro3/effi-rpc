package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.config.RouterConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.util.CollectionUtil;

import java.net.InetSocketAddress;

/**
 * Bootstrap class for initializing and configuring EffiRpc framework.
 */
public class EffiRpcBootstrap extends ScopedApplication.Holder {

    EffiRpcBootstrap(ScopedApplication application) {
        super(application);
    }

    public static EffiRpcBootstrap newInstance(ScopedPlatform platform, String applicationName) {
        return newInstance(platform.newApplication(applicationName));
    }

    /**
     * Creates a new instance of EffiRpcBootstrap with the specified application id.
     *
     * @param applicationName the id of the application
     * @return a new instance of EffiRpcBootstrap
     */
    public static EffiRpcBootstrap newInstance(String applicationName) {
        return newInstance(ScopedPlatform.defaultInstance(), applicationName);
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

    public EffiRpcBootstrap server(ServerConfig serverConfig, int port) {
        ServerLauncher.attach(application, serverConfig, port);
        return this;
    }

    public EffiRpcBootstrap server(ServerConfig serverConfig, String host, int port) {
        ServerLauncher.attach(application, serverConfig, host, port);
        return this;
    }

    public EffiRpcBootstrap server(ServerConfig serverConfig, InetSocketAddress boundAddress) {
        ServerLauncher.attach(application, serverConfig, boundAddress);
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
        AnnotationServantGroup.builder()
                .service(service)
                .module(application.defaultModule())
                .build();
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

    public EffiRpcBootstrap router(RouterConfig routerConfig) {
        application.defaultModule().registry().register(RouterConfig.class, routerConfig);
        return this;
    }

    /**
     * Starts the EffiRpc application.
     */
    public Future<Void> start() {
        application.start();
        ApplicationServiceRegistrar coordinator =
                application.singleComponent(ApplicationServiceRegistrar.class);
        if (coordinator == null) {
            coordinator = new ApplicationServiceRegistrar(application);
        }
        Future<Void> startup = coordinator.register();
        startup.onComplete(result -> {
            if (result.failed()) {
                application.close();
            }
        });
        return startup;
    }

    /**
     * Stops the EffiRpc application.
     */
    public EffiRpcBootstrap stop() {
        application.close();
        return this;
    }

    public boolean ready() {
        ApplicationServiceRegistrar coordinator =
                application.singleComponent(ApplicationServiceRegistrar.class);
        return application.active() && coordinator != null && coordinator.active();
    }

    public boolean live() {
        return application.active();
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
