package io.effi.rpc.boot;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.governance.discovery.ServiceDiscovery;
import io.effi.rpc.governance.loadbalance.LoadBalancer;
import io.effi.rpc.governance.router.Router;
import io.effi.rpc.registry.RegistryFactory;
import io.effi.rpc.registry.RegistryService;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves service URLs using ServiceDiscovery, Router, and LoadBalancer.
 */
public final class RegistryLocator implements Locator {

    private static final Map<String, RegistryLocator> RESOURCES = new ConcurrentHashMap<>();

    private final String remoteApplication;

    private RegistryLocator(String remoteApplication) {
        this.remoteApplication = AssertUtil.notBlank(remoteApplication, "remoteApplication");
    }

    public static RegistryLocator getInstance(String remoteApplication) {
        AssertUtil.notBlank(remoteApplication, "remoteApplication");
        return RESOURCES.computeIfAbsent(remoteApplication, k -> new RegistryLocator(remoteApplication));
    }

    public void preloadDiscoveries(Caller<?> caller) {
        for (RegistryConfig registryConfig : caller.registryConfigs()) {
            RegistryService registryService = ExtensionLoader.loadExtension(RegistryFactory.class, registryConfig.type())
                    .getService(registryConfig);
            registryService.discover(remoteApplication, caller.module());
        }
    }

    @Override
    public InetSocketAddress locate(InvocationContext<Envelope.Request, Caller<?>> context) {
        context.envelope().url().address(remoteApplication);
        Config config = context.invoker().config();
        // ServiceDiscovery
        // todo 优化
        ServiceDiscovery serviceDiscovery = ExtensionLoader.loadAdaptiveExtension(ServiceDiscovery.class, config::get);
        List<RegistryConfig> registryConfigs = context.invoker().registryConfigs();
        List<URL> availableServiceUrls = serviceDiscovery.discover(remoteApplication, context, registryConfigs);
        // Router
        Router router = ExtensionLoader.loadAdaptiveExtension(Router.class, config::get);

        List<URL> finalServiceUrls = router.route(context, availableServiceUrls);
        // LoadBalance
        LoadBalancer loadBalancer = ExtensionLoader.loadAdaptiveExtension(LoadBalancer.class, config::get);
        URL chosenUrl = loadBalancer.select(context, finalServiceUrls);
        return NetUtil.toInetSocketAddress(chosenUrl.address());
    }
}
