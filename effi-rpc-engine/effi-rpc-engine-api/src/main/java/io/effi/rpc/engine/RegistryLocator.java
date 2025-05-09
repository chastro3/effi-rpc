package io.effi.rpc.engine;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.governance.discovery.ServiceDiscovery;
import io.effi.rpc.governance.loadbalance.LoadBalancer;
import io.effi.rpc.governance.router.Router;
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
public class RegistryLocator implements Locator {

    private static final Map<String, RegistryLocator> RESOURCES = new ConcurrentHashMap<>();

    private final String remoteApplication;

    private RegistryLocator(String remoteApplication) {
        this.remoteApplication = AssertUtil.notBlank(remoteApplication, "remoteApplication");
    }

    public static RegistryLocator getInstance(String remoteApplication) {
        AssertUtil.notBlank(remoteApplication, "remoteApplication");
        return RESOURCES.computeIfAbsent(remoteApplication, k -> new RegistryLocator(remoteApplication));
    }

    @Override
    public InetSocketAddress locate(InvocationContext<Envelope.Request, Caller<?>> context) {
        context.envelope().url().address(remoteApplication);
        Config config = context.invoker().config();
        // ServiceDiscovery
        ServiceDiscovery serviceDiscovery = ExtensionLoader.loadExtension(ServiceDiscovery.class, config);
        URL[] registryConfigUrls = registryConfigs(context);
        List<URL> availableServiceUrls = serviceDiscovery.discover(context, registryConfigUrls);
        // Router
        EffiRpcModule module = context.module();
        Router router = module.get(Router.ATTRIBUTE_KEY);
        if (router == null) {
            router = ExtensionLoader.loadExtension(Router.class);
        }
        List<URL> finalServiceUrls = router.route(context, availableServiceUrls);
        // LoadBalance
        LoadBalancer loadBalancer = ExtensionLoader.loadExtension(LoadBalancer.class, config);
        URL chosenUrl = loadBalancer.select(context, finalServiceUrls);
        return NetUtil.toInetSocketAddress(chosenUrl.address());
    }

    private URL[] registryConfigs(InvocationContext<?, Caller<?>> context) {
        List<RegistryConfig> registryConfigs = context.invoker().registryConfigs();
        return registryConfigs.stream().map(config -> config.url().replicate()).toArray(URL[]::new);
    }
}
