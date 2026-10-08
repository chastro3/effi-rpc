package io.effi.rpc.core;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.governance.registry.ServiceRegistrar;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

/**
 * Manages application server registration and deregistration.
 */
@ScopedComponent(scope = APPLICATION, kind = SINGLE)
public class ApplicationServiceRegistrar extends ScopedApplication.Holder implements ServiceRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceRegistrar.class);

    private final List<ServerLauncher> serverLaunchers;

    private final List<RegistryConfig> registryConfigs;

    private final Object lifecycleLock = new Object();

    private volatile List<ServiceInstance> serviceInstances;

    private volatile State state = State.NEW;

    private Promise<Void> registrationFuture;

    private Promise<Void> deregistrationFuture;

    public ApplicationServiceRegistrar(ScopedApplication application) {
        super(application);
        this.serverLaunchers = lookupServerLaunchers();
        this.registryConfigs = lookupRegistryConfigs();
        application.registry().register(ApplicationServiceRegistrar.class, this);
    }

    /**
     * Creates a registrar for the supplied application.
     *
     * @param application owning application
     * @return new registrar
     */
    public static ApplicationServiceRegistrar forApplication(ScopedApplication application) {
        return new ApplicationServiceRegistrar(application);
    }

    @Override
    public Future<Void> register() {
        Promise<Void> result;
        synchronized (lifecycleLock) {
            if (state == State.READY) {
                return Futures.completedVoid();
            }
            if (state == State.STARTING) {
                return registrationFuture;
            }
            if (state == State.STOPPING || state == State.CLOSED) {
                return Promise.failed(applicationClosed());
            }
            result = new Promise<>();
            registrationFuture = result;
            state = State.STARTING;
        }
        startRegistration(result);
        return result;
    }

    @Override
    public Future<Void> deregister() {
        Promise<Void> result = new Promise<>();
        Future<Void> currentRegistration = null;
        synchronized (lifecycleLock) {
            if (state == State.NEW) {
                state = State.CLOSED;
                return Futures.completedVoid();
            }
            if (state == State.CLOSED) {
                return Futures.completedVoid();
            }
            if (state == State.STOPPING) {
                return deregistrationFuture;
            }

            boolean wasStarting = state == State.STARTING;
            state = State.STOPPING;
            deregistrationFuture = result;
            if (wasStarting) {
                currentRegistration = registrationFuture;
            }
        }

        if (currentRegistration == null) {
            stop(result);
        } else {
            currentRegistration.onComplete(ignored -> stop(result));
        }
        return result;
    }

    @Override
    public boolean active() {
        return state == State.READY;
    }

    @Override
    public List<ServiceInstance> serviceInstances() {
        return serviceInstances;
    }

    @Override
    public List<RegistryConfig> registryConfigs() {
        return registryConfigs;
    }

    /**
     * Attaches a server bound to the local host with default weight.
     *
     * @param config server configuration
     * @param port bound port
     * @return this registrar
     */
    public ApplicationServiceRegistrar server(ServerConfig config, int port) {
        return server(config, NetUtil.localHost(), port, 1);
    }

    /**
     * Attaches a weighted server bound to the local host.
     *
     * @param config server configuration
     * @param port bound port
     * @param weight server weight
     * @return this registrar
     */
    public ApplicationServiceRegistrar server(ServerConfig config, int port, int weight) {
        return server(config, NetUtil.localHost(), port, weight);
    }

    /**
     * Attaches a server bound to the supplied host with default weight.
     *
     * @param config server configuration
     * @param host bound host
     * @param port bound port
     * @return this registrar
     */
    public ApplicationServiceRegistrar server(ServerConfig config, String host, int port) {
        return server(config, host, port, 1);
    }

    /**
     * Attaches a weighted server bound to the supplied host.
     *
     * @param config server configuration
     * @param host bound host
     * @param port bound port
     * @param weight server weight
     * @return this registrar
     */
    public ApplicationServiceRegistrar server(ServerConfig config, String host, int port, int weight) {
        return server(config, InetSocketAddress.createUnresolved(host, port), weight);
    }

    /**
     * Attaches a server bound to the supplied address with default weight.
     *
     * @param config server configuration
     * @param boundAddress bound address
     * @return this registrar
     */
    public ApplicationServiceRegistrar server(ServerConfig config, InetSocketAddress boundAddress) {
        return server(config, boundAddress, 1);
    }

    /**
     * Attaches a weighted server bound to the supplied address.
     *
     * @param config server configuration
     * @param boundAddress bound address
     * @param weight server weight
     * @return this registrar
     */
    public ApplicationServiceRegistrar server(ServerConfig config, InetSocketAddress boundAddress, int weight) {
        synchronized (lifecycleLock) {
            requireConfigurable();
            ServerLauncher serverLauncher = ServerLauncher.attach(application, config, boundAddress, weight);
            serverLaunchers.add(serverLauncher);
        }
        return this;
    }

    /**
     * Registers additional registry configurations.
     *
     * @param configs registry configurations
     * @return this registrar
     */
    public ApplicationServiceRegistrar registry(RegistryConfig... configs) {
        synchronized (lifecycleLock) {
            requireConfigurable();
            if (CollectionUtil.isNotEmpty(configs)) {
                for (RegistryConfig config : configs) {
                    application.platform().registry().register(RegistryConfig.class, config);
                    registryConfigs.add(config);
                }
            }
        }
        return this;
    }

    /**
     * Returns the attached server launchers.
     */
    public List<ServerLauncher> serverLaunchers() {
        return serverLaunchers;
    }

    private List<ServerLauncher> lookupServerLaunchers() {
        return new ArrayList<>(application.components(ServerLauncher.class, (name, item) -> !item.active()));
    }

    private List<RegistryConfig> lookupRegistryConfigs() {
        return new ArrayList<>(platform().components(RegistryConfig.class,
                (name, item) -> item.hasTags(Tags.PROVIDER, Tags.FORCE_ACTIVE)));
    }

    private void startRegistration(Promise<Void> result) {
        AtomicBoolean registrationStarted = new AtomicBoolean(false);
        Future<Void> startup;
        List<ServiceInstance> instances;
        try {
            instances = createServiceInstances();
            startup = Futures.compose(bindServers(), ignored -> {
                registrationStarted.set(true);
                return registerServiceInstances(instances);
            });
        } catch (Throwable cause) {
            failRegistration(result, cause);
            return;
        }

        startup.onComplete(outcome -> {
            if (outcome.succeeded()) {
                completeRegistration(result, instances);
            } else {
                rollbackRegistration(result, instances, registrationStarted.get(), outcome.cause());
            }
        });
    }

    private List<ServiceInstance> createServiceInstances() {
        if (CollectionUtil.isEmpty(serverLaunchers)) return Collections.emptyList();
        Map<String, String> metadata = new HashMap<>();
        metadata.put(KeyConstant.PLATFORM, platform().name());
        metadata.put(KeyConstant.APPLICATION, application.name());
        return serverLaunchers.stream()
                .map(serverLauncher -> (ServiceInstance) DefaultServiceInstance.builder()
                        .id(serverLauncher.id())
                        .serviceName(application().name())
                        .protocol(serverLauncher.serverConfig().protocolName())
                        .host(serverLauncher.boundAddress().getHostString())
                        .port(serverLauncher.boundAddress().getPort())
                        .addMetadata(metadata)
                        .addMetadata(KeyConstant.WEIGHT, String.valueOf(serverLauncher.weight()))
                        .build())
                .toList();
    }

    private Future<Void> bindServers() {
        List<Future<?>> bindFutures = new ArrayList<>(serverLaunchers.size());
        for (ServerLauncher serverLauncher : serverLaunchers) {
            Server server = serverLauncher.start();
            bindFutures.add(server.bind());
        }
        return Futures.allOf(bindFutures);
    }

    private Future<Void> registerServiceInstances(List<ServiceInstance> serviceInstances) {
        if (CollectionUtil.isEmpty(registryConfigs)) {
            logger.warn("No available registry config(s)");
            return Futures.completedVoid();
        }

        List<Future<Void>> futures = new ArrayList<>(registryConfigs.size());
        for (RegistryConfig registryConfig : registryConfigs) {
            RegistryClient registryClient = RegistryClient.of(registryConfig, platform());
            futures.add(registryClient.register(serviceInstances));
        }
        return Futures.allOf(futures);
    }

    private void completeRegistration(Promise<Void> result, List<ServiceInstance> instances) {
        boolean completed;
        synchronized (lifecycleLock) {
            serviceInstances = instances;
            completed = state == State.STARTING && registrationFuture == result;
            if (completed) {
                state = State.READY;
            }
        }
        if (completed) {
            result.success(null);
        } else {
            result.failure(applicationClosed());
        }
    }

    private void rollbackRegistration(
            Promise<Void> result,
            List<ServiceInstance> instances,
            boolean registrationStarted,
            Throwable cause
    ) {
        Future<Void> rollback = registrationStarted
                ? deregisterServiceInstances(instances)
                : Futures.completedVoid();
        rollback.onComplete(ignored -> failRegistration(result, cause));
    }

    private void failRegistration(Promise<Void> result, Throwable cause) {
        serverLaunchers.forEach(ServerLauncher::close);
        synchronized (lifecycleLock) {
            serviceInstances = null;
            if (state == State.STARTING && registrationFuture == result) {
                state = State.CLOSED;
            }
        }
        logger.error("Failed to start service server(s).", cause);
        result.failure(toRpcException(cause));
    }

    private void stop(Promise<Void> result) {
        deregisterServiceInstances(serviceInstances).onComplete(res -> {
            serviceInstances = null;
            serverLaunchers.forEach(ServerLauncher::close);
            synchronized (lifecycleLock) {
                state = State.CLOSED;
            }
            result.complete(res);
        });
    }

    private Future<Void> deregisterServiceInstances(List<ServiceInstance> instances) {
        if (CollectionUtil.isEmpty(instances) || CollectionUtil.isEmpty(registryConfigs)) {
            return Futures.completedVoid();
        }
        List<Future<Void>> futures = new ArrayList<>(registryConfigs.size());
        for (RegistryConfig registryConfig : registryConfigs) {
            RegistryClient registryClient = RegistryClient.of(registryConfig, platform());
            futures.add(registryClient.deregister(instances));
        }
        return Futures.allOf(futures);
    }

    private void requireConfigurable() {
        if (state != State.NEW) {
            throw new IllegalStateException("Application '" + application.name() + "' has already started");
        }
    }

    private EffiRpcException applicationClosed() {
        return PredefinedErrorCode.SERVICE_UNAVAILABLE.fail(application.name());
    }

    private EffiRpcException toRpcException(Throwable cause) {
        return cause instanceof EffiRpcException exception
                ? exception
                : PredefinedErrorCode.SERVICE_UNAVAILABLE.fail(cause, cause.getMessage());
    }

    private enum State {
        NEW,
        STARTING,
        READY,
        STOPPING,
        CLOSED
    }
}
