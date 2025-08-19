package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Locator;
import io.effi.rpc.context.LocatorFactory;
import io.effi.rpc.context.Request;
import io.effi.rpc.governance.lb.LoadBalancer;
import io.effi.rpc.governance.router.Router;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.RegistryClientFactory;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.ArrayIdentifier;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Pair;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.governance.registry.RegistryLocator.Factory.NAME;

/**
 * Resolves service URLs using ServiceDiscovery, Router, and LoadBalancer.
 */
public final class RegistryLocator implements Locator {

    private static final Logger logger = LoggerFactory.getLogger(RegistryLocator.class);

    private static final Map<Pair<String, ArrayIdentifier<String>>, RegistryLocator> CACHE = new ConcurrentHashMap<>();

    private final String remoteApplication;

    private final Caller<?> caller;

    private final boolean immutable;

    private final Collection<RegistryConfig> configuredRegistryConfigs;

    private RegistryLocator(String remoteApplication, Caller<?> caller, Collection<String> configuredRegistryNames) {
        this.remoteApplication = remoteApplication;
        this.caller = caller;
        this.immutable = caller.getConfig(ConfigNames.IMMUTABLE);
        this.configuredRegistryConfigs = initializeRegistryConfigs(configuredRegistryNames);
    }

    public static RegistryLocator from(String target, Caller<?> caller) {
        AssertUtil.notBlank(target, "target");
        AssertUtil.notNull(caller, "caller");
        Collection<String> configuredRegistryNames = CollectionUtil.flatDistinctArray(caller.getMergedConfig(ConfigNames.REGISTRY));
        ArrayIdentifier<String> identifier = ArrayIdentifier.of(configuredRegistryNames.toArray(StringUtil.emptyArray()));
        Pair<String, ArrayIdentifier<String>> key = Pair.of(target, identifier);
        return CACHE.computeIfAbsent(key, k ->
                new RegistryLocator(target, caller, configuredRegistryNames)
        );
    }

    public void preloadDiscoveries() {
        if (configuredRegistryConfigs.isEmpty()) {
            logger.warn("No registry config(s) found for '{}' registry locator", remoteApplication);
        }
        ScopedApplication application = caller.module().application();
        for (RegistryConfig registryConfig : configuredRegistryConfigs) {
            RegistryClient registryClient = application.namedExtension(RegistryClientFactory.class, registryConfig.type())
                    .fetch(registryConfig);
            registryClient.lookup(remoteApplication);
        }
    }

    @Override
    public InetSocketAddress locate(CallContext<Request, Caller<?>> context) {
        context.message().url();
        Config config = context.peer().config();
        ScopedApplication application = context.module().application();
        // ServiceDiscovery
        // todo 优化
        ServiceDiscovery serviceDiscovery = application.adaptiveExtension(ServiceDiscovery.class, config::get);
        Collection<RegistryConfig> registryConfigs = availableRegistryConfigs();
        List<ServiceInstance> availableServiceInstances = serviceDiscovery.discover(remoteApplication, context, registryConfigs);
        // Router
        Router router = application.adaptiveExtension(Router.class, config::get);

        List<ServiceInstance> finalServiceInstances = router.route(context, availableServiceInstances);
        // LoadBalance
        LoadBalancer loadBalancer = application.adaptiveExtension(LoadBalancer.class, config::get);
        ServiceInstance chosenServiceInstance = loadBalancer.select(context, finalServiceInstances);
        return InetSocketAddress.createUnresolved(chosenServiceInstance.host(), chosenServiceInstance.port());
    }

    public boolean immutable() {
        return immutable;
    }

    private Collection<RegistryConfig> initializeRegistryConfigs(Collection<String> configuredNames) {
        return immutable ? mergeRegistryConfigs(findConfiguredRegistryConfigs(configuredNames))
                : findConfiguredRegistryConfigs(configuredNames);
    }

    private Collection<RegistryConfig> availableRegistryConfigs() {
        return immutable ? configuredRegistryConfigs
                : mergeRegistryConfigs(configuredRegistryConfigs);
    }

    private Collection<RegistryConfig> mergeRegistryConfigs(Collection<RegistryConfig> configuredRegistryConfigs) {
        Collection<RegistryConfig> platformRegistryConfigs = findPlatformRegistryConfigs();
        return CollectionUtil.merge(configuredRegistryConfigs, platformRegistryConfigs);
    }

    private Collection<RegistryConfig> findConfiguredRegistryConfigs(Collection<String> configuredNames) {
        if (CollectionUtil.isEmpty(configuredNames)) return Collections.emptyList();
        return caller.platform().components(RegistryConfig.class,
                (name, item) -> configuredNames.contains(name)
        );
    }

    private Collection<RegistryConfig> findPlatformRegistryConfigs() {
        return caller.platform().components(RegistryConfig.class,
                (name, item) -> item.hasTags(Tags.CONSUMER, Tags.FORCE_ACTIVE)
        );
    }

    @Extension(value = NAME, primary = true)
    public static class Factory implements LocatorFactory {

        public static final String NAME = "registry";

        @Override
        public Locator fetch(String target, Caller<?> caller) {
            return RegistryLocator.from(target, caller);
        }
    }

}
