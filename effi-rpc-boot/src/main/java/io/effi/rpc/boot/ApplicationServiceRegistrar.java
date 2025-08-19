package io.effi.rpc.boot;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.async.Future;
import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.governance.registry.ServiceRegistrar;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.RegistryClientFactory;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.CollectionUtil;

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

    private final List<ServiceInstance> serviceInstances;

    private final AtomicBoolean active = new AtomicBoolean(false);

    public ApplicationServiceRegistrar(ScopedApplication application) {
        super(application);
        this.serverLaunchers = lookupServerLaunchers();
        this.registryConfigs = lookupRegistryConfigs();
        this.serviceInstances = createServiceInstances();
        application.registry().register(ApplicationServiceRegistrar.class, this);
    }

    @Override
    public void register() {
        if (active.compareAndSet(false, true)) {
            for (int i = 0; i < serverLaunchers.size(); i++) {
                ServerLauncher serverLauncher = serverLaunchers.get(i);
                Server server = serverLauncher.start();
                ServiceInstance serviceInstance = serviceInstances.get(i);
                if (server.isActive()) {
                    registerServiceInstance(serviceInstance);
                } else {
                    server.bind().onComplete(res -> {
                        if (res.succeeded()) {
                            registerServiceInstance(serviceInstance);
                        }
                    });
                }
            }
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
    public boolean isActive() {
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

    public List<ServerLauncher> serverLaunchers() {
        return serverLaunchers;
    }

    private List<ServerLauncher> lookupServerLaunchers() {
        return List.copyOf(application.components(ServerLauncher.class));
    }

    private List<RegistryConfig> lookupRegistryConfigs() {
        return List.copyOf(platform().components(RegistryConfig.class,
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

    private RegistryClient fetchRegistryClient(RegistryConfig config) {
        var factory = platform().namedExtension(RegistryClientFactory.class, config.type());
        return factory.fetch(config);
    }

    private void registerServiceInstance(ServiceInstance serviceInstance) {
        if (CollectionUtil.isEmpty(registryConfigs)) {
            logger.warn("No available registry config(s)");
        } else {
            for (RegistryConfig registryConfig : registryConfigs) {
                RegistryClient registryClient = fetchRegistryClient(registryConfig);
                registryClient.register(serviceInstance);
            }
        }

    }

    private Future<Void> deregisterServiceInstances() {
        List<Future<Void>> futures = new ArrayList<>();
        for (RegistryConfig registryConfig : registryConfigs) {
            RegistryClient registryClient = fetchRegistryClient(registryConfig);
            for (ServiceInstance serviceInstance : serviceInstances) {
                futures.add(registryClient.deregister(serviceInstance));
            }
        }
        return Promise.allOf(futures);
    }
}
