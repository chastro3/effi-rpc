package io.effi.rpc.boot;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.governance.registry.ServiceRegistrar;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.concurrent.Promise;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

@ScopedComponent(scope = APPLICATION, kind = SINGLE)
public class ApplicationServiceRegistrar extends ScopedApplication.Holder implements ServiceRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceRegistrar.class);

    private final List<ServerLauncher> serverLaunchers;

    private final List<RegistryConfig> registryConfigs;

    private List<ServiceInstance> serviceInstances;

    private final AtomicBoolean active = new AtomicBoolean(false);

    public static ApplicationServiceRegistrar forApplication(ScopedApplication application) {
        return new ApplicationServiceRegistrar(application);
    }

    public ApplicationServiceRegistrar(ScopedApplication application) {
        super(application);
        this.serverLaunchers = lookupServerLaunchers();
        this.registryConfigs = lookupRegistryConfigs();
        application.registry().register(ApplicationServiceRegistrar.class, this);
    }

    @Override
    public void register() {
        if (active.compareAndSet(false, true)) {
            List<ServiceInstance> serviceInstances = createServiceInstances();
            for (int i = 0; i < serverLaunchers.size(); i++) {
                ServerLauncher serverLauncher = serverLaunchers.get(i);
                Server server = serverLauncher.start();
                ServiceInstance serviceInstance = serviceInstances.get(i);
                if (server.active()) {
                    registerServiceInstance(serviceInstance);
                } else {
                    server.bind().onComplete(res -> {
                        if (res.succeeded()) {
                            registerServiceInstance(serviceInstance);
                        }
                    });
                }
            }
            this.serviceInstances = serviceInstances;
        }
    }

    @Override
    public void deregister() {
        if (active.compareAndSet(true, false)) {
            deregisterServiceInstances()
                    .onComplete(res ->  serverLaunchers.forEach(ServerLauncher::close));
        }
    }

    @Override
    public boolean active() {
        return active.get();
    }

    @Override
    public List<ServiceInstance> serviceInstances() {
        return serviceInstances;
    }

    @Override
    public List<RegistryConfig> registryConfigs() {
        return registryConfigs;
    }

    public ApplicationServiceRegistrar attachServer(ServerConfig config, int port) {
        return attachServer(config, NetUtil.localHost(), port);
    }

    public ApplicationServiceRegistrar attachServer(ServerConfig config, String host, int port) {
        return attachServer(config, InetSocketAddress.createUnresolved(host, port));
    }

    public ApplicationServiceRegistrar attachServer(ServerConfig config, InetSocketAddress boundAddress) {
        if (!active()) {
            ServerLauncher serverLauncher = ServerLauncher.attach(application, config, boundAddress);
            serverLaunchers.add(serverLauncher);
        }
        return this;
    }

    public ApplicationServiceRegistrar registry(RegistryConfig... configs) {
        if (!active()) {
            if (CollectionUtil.isNotEmpty(configs)) {
                for (RegistryConfig config : configs) {
                    application.platform().registry().register(RegistryConfig.class, config);
                    registryConfigs.add(config);
                }
            }
        }
        return this;
    }

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

    private List<ServiceInstance> createServiceInstances() {
        if (CollectionUtil.isEmpty(serverLaunchers)) return Collections.emptyList();
        Map<String, String> metadata = new HashMap<>();
        metadata.put(KeyConstant.PLATFORM, platform().name());
        metadata.put(KeyConstant.APPLICATION, application.name());
        return serverLaunchers.stream()
                .map(serverLauncher -> (ServiceInstance)
                        DefaultServiceInstance.builder()
                                .id(serverLauncher.id())
                                .serviceName(application().name())
                                .protocol(serverLauncher.serverConfig().protocolName())
                                .host(serverLauncher.boundAddress().getHostString())
                                .port(serverLauncher.boundAddress().getPort())
                                .addMetadata(metadata)
                                .build())
                .toList();
    }

    private void registerServiceInstance(ServiceInstance serviceInstance) {
        if (CollectionUtil.isEmpty(registryConfigs)) {
            logger.warn("No available registry config(s)");
        } else {
            for (RegistryConfig registryConfig : registryConfigs) {
                RegistryClient registryClient = RegistryClient.of(registryConfig, platform());
                registryClient.register(serviceInstance);
            }
        }

    }

    private Future<Void> deregisterServiceInstances() {
        List<Future<Void>> futures = new ArrayList<>();
        for (RegistryConfig registryConfig : registryConfigs) {
            RegistryClient registryClient = RegistryClient.of(registryConfig, platform());
            for (ServiceInstance serviceInstance : serviceInstances) {
                futures.add(registryClient.deregister(serviceInstance));
            }
        }
        return Promise.allOf(futures);
    }
}
