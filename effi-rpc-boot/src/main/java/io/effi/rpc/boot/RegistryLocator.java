package io.effi.rpc.boot;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.governance.discovery.ServiceDiscovery;
import io.effi.rpc.governance.loadbalance.LoadBalancer;
import io.effi.rpc.governance.router.Router;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryFactory;
import io.effi.rpc.registry.RegistryService;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Resolves service URLs using ServiceDiscovery, Router, and LoadBalancer.
 */
public final class RegistryLocator implements Locator {

    private static final Logger logger = LoggerFactory.getLogger(RegistryLocator.class);

    private final Caller<?> caller;

    private final String remoteApplication;

    private final List<RegistryConfig> registryConfigs;

    public RegistryLocator(Caller<?> caller, String remoteApplication, List<RegistryConfig> registryConfigs) {
        this.caller = AssertUtil.notNull(caller, "caller");
        this.remoteApplication = AssertUtil.notBlank(remoteApplication, "remote application name");
        this.registryConfigs = new ArrayList<>(registryConfigs);
        addConfiguredRegistryConfigs();
    }

    public RegistryLocator addRegistryConfig(Collection<RegistryConfig> registryConfigs) {
        if (CollectionUtil.isNotEmpty(registryConfigs)) {
            CollectionUtil.addUnique(this.registryConfigs, registryConfigs);
        }
        return this;
    }

    public void preloadDiscoveries() {
        if (registryConfigs.isEmpty()) {
            logger.warn("No registry config(s) found for '{}' registry locator", remoteApplication);
        }
        EffiRpcApplication application = caller.module().application();
        for (RegistryConfig registryConfig : registryConfigs) {
            RegistryService registryService = application.getExtension(RegistryFactory.class, registryConfig.type())
                    .getService(registryConfig);
            registryService.discover(remoteApplication, caller.module());
        }
    }

    @Override
    public InetSocketAddress locate(CallContext<Message.Request, Caller<?>> context) {
        context.message().url().address(remoteApplication);
        Config config = context.callSide().config();
        EffiRpcApplication application = context.module().application();
        // ServiceDiscovery
        // todo 优化
        ServiceDiscovery serviceDiscovery = application.getAdaptiveExtension(ServiceDiscovery.class, config::get);
        List<URL> availableServiceUrls = serviceDiscovery.discover(remoteApplication, context, registryConfigs);
        // Router
        Router router = application.getAdaptiveExtension(Router.class, config::get);

        List<URL> finalServiceUrls = router.route(context, availableServiceUrls);
        // LoadBalance
        LoadBalancer loadBalancer = application.getAdaptiveExtension(LoadBalancer.class, config::get);
        URL chosenUrl = loadBalancer.select(context, finalServiceUrls);
        return NetUtil.toInetSocketAddress(chosenUrl.address());
    }

    private void addConfiguredRegistryConfigs() {
        List<String> configuredNames = caller.getCascadedConfig(DefaultConfigNames.REGISTRY);
        Collection<RegistryConfig> registryConfigs = caller.application().listOf(RegistryConfig.class,
                (name, item) -> configuredNames.contains(name) || item.hasTags(Tags.CONSUMER, Tags.FORCE_ACTIVE)
        );
        addRegistryConfig(registryConfigs);
    }
}
