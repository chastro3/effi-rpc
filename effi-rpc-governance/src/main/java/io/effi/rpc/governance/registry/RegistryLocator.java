package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Locator;
import io.effi.rpc.context.LocatorResolver;
import io.effi.rpc.context.PeerDescriptor;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.governance.lb.LoadBalancer;
import io.effi.rpc.governance.router.Router;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.effi.rpc.governance.registry.RegistryLocator.Resolver.NAME;

/**
 * Locates a service address by composing discovery, routing, and load balancing.
 */
public final class RegistryLocator implements Locator {

    private static final Map<CacheKey, RegistryLocator> CACHE = new ConcurrentHashMap<>();

    private final AtomicBoolean preloaded = new AtomicBoolean();

    private final ScopedPlatform platform;

    private final String serviceName;

    private final Collection<RegistryConfig> registryConfigs;

    private RegistryLocator(ScopedPlatform platform, String serviceName, Collection<RegistryConfig> registryConfigs) {
        this.platform = platform;
        this.serviceName = serviceName;
        this.registryConfigs = registryConfigs;
    }

    /**
     * Returns the shared locator for the supplied registry configurations.
     *
     * @param platform    the owning platform
     * @param serviceName the service name to discover
     * @param configs     the registry configurations
     * @return the cached locator
     */
    public static RegistryLocator cached(ScopedPlatform platform, String serviceName, RegistryConfig... configs) {
        return cached(platform, serviceName, List.of(configs));
    }

    /**
     * Returns the shared locator for the supplied registry configurations.
     *
     * @param platform    the owning platform
     * @param serviceName the service name to discover
     * @param configs     the registry configurations
     * @return the cached locator
     */
    public static RegistryLocator cached(ScopedPlatform platform, String serviceName, Collection<RegistryConfig> configs) {
        AssertUtil.notNull(platform, "platform");
        if (CollectionUtil.isEmpty(configs)) throw new IllegalArgumentException("registry config(s) cannot be empty");
        Map<String, RegistryConfig> configsById = new TreeMap<>();
        for (RegistryConfig config : configs) {
            configsById.putIfAbsent(config.id(), config);
        }
        CacheKey key = new CacheKey(platform, serviceName, configsById.keySet());
        return CACHE.computeIfAbsent(key, k ->
                new RegistryLocator(platform, serviceName, configsById.values())
        );
    }

    static void evict(ScopedPlatform platform) {
        CACHE.keySet().removeIf(key -> key.platform() == platform);
    }

    /**
     * Starts discovery for every registry config at most once per locator.
     */
    public void preloadDiscoveries() {
        if (!preloaded.compareAndSet(false, true)) return;
        for (RegistryConfig registryConfig : registryConfigs) {
            RegistryClient registryClient = RegistryClient.of(registryConfig, platform);
            registryClient.lookup(serviceName);
        }
    }

    @Override
    public InetSocketAddress locate(CallContext<Request, Caller<?>> context) {
        Caller<?> caller = context.peer();
        ScopedApplication application = context.module().application();
        ServiceDiscovery serviceDiscovery = application.preferredExtension(ServiceDiscovery.class, caller.option(GovernanceOptions.SERVICE_DISCOVERY));
        List<ServiceInstance> availableServiceInstances = serviceDiscovery.discover(serviceName, context, registryConfigs);
        Router router = application.preferredExtension(Router.class, caller.option(GovernanceOptions.ROUTER));
        List<ServiceInstance> finalServiceInstances = router.route(context, availableServiceInstances);
        if (CollectionUtil.isEmpty(finalServiceInstances)) {
            throw InteractionErrorCodes.ROUTE_NOT_MATCHED.fail(context.message().url());
        }
        LoadBalancer loadBalancer = application.preferredExtension(LoadBalancer.class, caller.option(GovernanceOptions.LOAD_BALANCER));
        ServiceInstance chosenServiceInstance = loadBalancer.select(context, finalServiceInstances);
        return InetSocketAddress.createUnresolved(chosenServiceInstance.host(), chosenServiceInstance.port());
    }

    @Extension(value = NAME, primary = true)
    public static class Resolver implements LocatorResolver {

        public static final String NAME = "registry";

        @Override
        public Locator resolve(PeerDescriptor descriptor, ScopedPlatform platform) {
            Collection<String> configuredNames = CollectionUtil.toHashSet(
                    descriptor.options().option(GovernanceOptions.REGISTRY)
            );
            Collection<RegistryConfig> registryConfigs = platform
                    .components(RegistryConfig.class, (name, registryConfig) ->
                            configuredNames.contains(name) || registryConfig.hasTags(Tags.FORCE_ACTIVE)
                    );
            String endpoint = descriptor.options().option(CallerOptions.ENDPOINT);
            RegistryLocator locator = RegistryLocator.cached(platform, endpoint, registryConfigs);
            locator.preloadDiscoveries();
            return locator;
        }
    }

    private record CacheKey(ScopedPlatform platform, String serviceName, Set<String> configIds) {
    }

}
