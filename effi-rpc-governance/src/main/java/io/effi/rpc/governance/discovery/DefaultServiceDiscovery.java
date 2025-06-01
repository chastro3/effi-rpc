package io.effi.rpc.governance.discovery;

import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryFactory;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Provides the default implementation of {@link ServiceDiscovery}.
 * <p>Deduplication based on address.</p>
 */
@Extension(Component.DEFAULT)
public class DefaultServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceDiscovery.class);

    @Override
    public List<URL> discover(String serviceName, InvocationContext<Envelope.Request, Caller<?>> context, List<RegistryConfig> registryConfigs) {
        if (CollectionUtil.isEmpty(registryConfigs)) {
            logger.warn("Registry config(s) is empty");
        }
        List<URL> availableServiceUrls = new ArrayList<>();
        URL url = context.envelope().url();
        int size = registryConfigs.size();
        CompletableFuture<List<URL>>[] futures = ObjectUtil.newFutureArray(size);
        for (int i = 0; i < size; i++) {
            RegistryConfig registryConfig = registryConfigs.get(i);
            var registryService = ExtensionLoader.loadExtension(RegistryFactory.class, registryConfig.type())
                    .getService(registryConfig);
            futures[i] = registryService.discover(serviceName, context.module());
        }
        CompletableFuture.allOf(futures).join();
        for (CompletableFuture<List<URL>> future : futures) {
            List<URL> discoverUrls = future.join();
            if (CollectionUtil.isNotEmpty(discoverUrls)) {
                for (URL discoverUrl : discoverUrls) {
                    if (discoverUrl.protocol().equals(url.protocol())) {
                        CollectionUtil.addToList(
                                availableServiceUrls,
                                (existUrl, newUrl) -> Objects.equals(existUrl.address(), newUrl.address()),
                                discoverUrl);
                    }
                }
            }
        }
        if (availableServiceUrls.isEmpty()) {
            throw PredefinedErrorCode.NOT_FOUND_SERVICE.fail(null, url);
        }
        return availableServiceUrls;
    }
}
