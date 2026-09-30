package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.config.RouterConfig;
import io.effi.rpc.option.HierarchicalOptions;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

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
     * Provides one annotation-based service.
     *
     * @param service the service instance
     * @return the updated EffiRpcBootstrap instance
     */
    public EffiRpcBootstrap provide(Object service) {
        AnnotationServantGroup.builder()
                .service(service)
                .module(application.defaultModule())
                .build();
        return this;
    }

    /**
     * Provides one interface-based service.
     */
    public <T> EffiRpcBootstrap provide(Class<T> targetType, T service, Consumer<HierarchicalOptions> customizer) {
        InterfaceServantGroup.<T>builder()
                .targetType(targetType)
                .service(service)
                .options(options(customizer))
                .module(application.defaultModule())
                .build();
        return this;
    }

    /**
     * Creates one caller proxy. Annotation callers are detected by {@link CallGroup}.
     */
    public <T> T consume(Class<T> targetType) {
        return AnnotationCallerGroup.<T>builder()
                .targetType(targetType)
                .module(application.defaultModule())
                .build()
                .proxy();
    }

    /**
     * Creates one interface-based caller proxy.
     */
    public <T> T consume(Class<T> targetType, Consumer<HierarchicalOptions> customizer) {
        return InterfaceCallerGroup.<T>builder()
                .targetType(targetType)
                .options(options(customizer))
                .module(application.defaultModule())
                .build()
                .proxy();
    }

    private static HierarchicalOptions options(Consumer<HierarchicalOptions> customizer) {
        HierarchicalOptions options = HierarchicalOptions.create();
        if (customizer != null) {
            customizer.accept(options);
        }
        return options;
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
