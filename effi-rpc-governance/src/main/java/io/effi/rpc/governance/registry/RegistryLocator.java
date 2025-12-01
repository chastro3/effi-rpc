package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Locator;
import io.effi.rpc.context.Request;
import io.effi.rpc.governance.lb.LoadBalancer;
import io.effi.rpc.governance.router.Router;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.ArrayIdentifier;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Pair;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.config.OptionName.Strategy.MERGE_PARENT;
import static io.effi.rpc.governance.registry.RegistryLocator.Factory.NAME;

/**
 * Resolves service URLs using ServiceDiscovery, Router, and LoadBalancer.
 */
public final class RegistryLocator implements Locator {

    public static final OptionName<String[]> REGISTRY = ConfigurableOptionName.nameOf("registry", MERGE_PARENT);

    private static final Logger logger = LoggerFactory.getLogger(RegistryLocator.class);

    private static final Map<Pair<String, ArrayIdentifier<RegistryConfig>>, RegistryLocator> CACHE = new ConcurrentHashMap<>();

    private final String serviceName;

    private final Collection<RegistryConfig> registryConfigs;

    private RegistryLocator(String serviceName, Collection<RegistryConfig> registryConfigs) {
        this.serviceName = serviceName;
        this.registryConfigs = registryConfigs;
    }

    public static RegistryLocator cached(String serviceName, RegistryConfig... configs) {
        return cached(serviceName, List.of(configs));
    }

    public static RegistryLocator cached(String serviceName, Collection<RegistryConfig> configs) {
        if (CollectionUtil.isEmpty(configs)) throw new IllegalArgumentException("registry config(s) cannot be empty");
        RegistryConfig[] array = configs.stream().distinct().toArray(RegistryConfig[]::new);
        ArrayIdentifier<RegistryConfig> identifier = ArrayIdentifier.of(array);
        Pair<String, ArrayIdentifier<RegistryConfig>> key = Pair.of(serviceName, identifier);
        return CACHE.computeIfAbsent(key, k ->
                new RegistryLocator(serviceName, List.of(array))
        );
    }

    public void preloadDiscoveries(ScopedPlatform platform) {
        if (registryConfigs.isEmpty()) {
            logger.warn("No registry config(s) found for '{}' registry locator", serviceName);
        }
        for (RegistryConfig registryConfig : registryConfigs) {
            RegistryClient registryClient = RegistryClient.of(registryConfig, platform);
            registryClient.lookup(serviceName);
        }
    }

    @Override
    public InetSocketAddress locate(CallContext<Request, Caller<?>> context) {
        context.message().url();
        Caller<?> caller = context.peer();
        ScopedApplication application = context.module().application();
        // ServiceDiscovery
        // todo 优化
        ServiceDiscovery serviceDiscovery = application.preferredExtension(ServiceDiscovery.class, caller.option(Caller.SERVICE_DISCOVERY));
        List<ServiceInstance> availableServiceInstances = serviceDiscovery.discover(serviceName, context, registryConfigs);
        // Router
        Router router = application.preferredExtension(Router.class, caller.option(Caller.ROUTER));
        List<ServiceInstance> finalServiceInstances = router.route(context, availableServiceInstances);
        // LoadBalance
        LoadBalancer loadBalancer = application.preferredExtension(LoadBalancer.class, caller.option(Caller.LOAD_BALANCER));
        ServiceInstance chosenServiceInstance = loadBalancer.select(context, finalServiceInstances);
        return InetSocketAddress.createUnresolved(chosenServiceInstance.host(), chosenServiceInstance.port());
    }

    @Extension(value = NAME, primary = true)
    public static class Factory implements Locator.Factory {

        public static final String NAME = "registry";

        @Override
        public Locator fetch(String endpoint, Caller<?> caller) {
            Collection<String> configuredNames = CollectionUtil.flatDistinctArray(caller.mergedOption(REGISTRY));
            Collection<RegistryConfig> registryConfigs = caller.platform()
                    .components(RegistryConfig.class, (name, registryConfig) ->
                            configuredNames.contains(name) || registryConfig.hasTags(Tags.FORCE_ACTIVE)
                    );
            return RegistryLocator.cached(endpoint, registryConfigs.toArray(RegistryConfig[]::new));
        }


    }

}
