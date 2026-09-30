package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
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
import io.effi.rpc.util.ArrayIdentifier;
import io.effi.rpc.util.CollectionUtil;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.effi.rpc.governance.registry.RegistryLocator.Resolver.NAME;

/**
 * Resolves service URLs using ServiceDiscovery, Router, and LoadBalancer.
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

    public static RegistryLocator cached(ScopedPlatform platform, String serviceName, RegistryConfig... configs) {
        return cached(platform, serviceName, List.of(configs));
    }

    public static RegistryLocator cached(ScopedPlatform platform, String serviceName, Collection<RegistryConfig> configs) {
        AssertUtil.notNull(platform, "platform");
        if (CollectionUtil.isEmpty(configs)) throw new IllegalArgumentException("registry config(s) cannot be empty");
        RegistryConfig[] array = configs.stream().distinct().toArray(RegistryConfig[]::new);
        CacheKey key = new CacheKey(platform, serviceName, ArrayIdentifier.of(array));
        return CACHE.computeIfAbsent(key, k ->
                new RegistryLocator(platform, serviceName, List.of(array))
        );
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
        // ServiceDiscovery
        // todo 优化
        ServiceDiscovery serviceDiscovery = application.preferredExtension(ServiceDiscovery.class, caller.option(GovernanceOptions.SERVICE_DISCOVERY));
        List<ServiceInstance> availableServiceInstances = serviceDiscovery.discover(serviceName, context, registryConfigs);
        // Router
        Router router = application.preferredExtension(Router.class, caller.option(GovernanceOptions.ROUTER));
        List<ServiceInstance> finalServiceInstances = router.route(context, availableServiceInstances);
        // LoadBalance
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

    private record CacheKey(ScopedPlatform platform, String serviceName, ArrayIdentifier<RegistryConfig> configs) {
    }

}
