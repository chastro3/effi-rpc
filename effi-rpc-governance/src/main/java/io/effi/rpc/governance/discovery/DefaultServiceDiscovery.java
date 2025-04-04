package io.effi.rpc.governance.discovery;

import io.effi.rpc.common.constant.Component;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.RegistryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link ServiceDiscovery}.
 * <p>Deduplication based on address.</p>
 */
@Extension(Component.DEFAULT)
public class DefaultServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceDiscovery.class);

    @Override
    public List<URL> discover(InvocationContext<?, Caller<?>> context, URL... registryConfigs) {
        if (CollectionUtil.isEmpty(registryConfigs)) {
            logger.warn("Registry config(s) is empty");
        }
        List<URL> availableServiceUrls = new ArrayList<>();
        URL url = context.source().url();
        EffRpcApplication application = context.module().application();
        for (URL registryUrl : registryConfigs) {
            var registryService = ExtensionLoader.loadExtension(RegistryFactory.class, registryUrl.protocol()).getService(application, registryUrl);
            List<URL> discoverUrls = registryService.discover(url);
            if (CollectionUtil.isNotEmpty(discoverUrls)) {
                for (URL discoverUrl : discoverUrls) {
                    CollectionUtil.addToList(
                            availableServiceUrls,
                            (existUrl, newUrl) -> Objects.equals(existUrl.address(), newUrl.address()),
                            discoverUrl);
                }
            }
        }
        if (availableServiceUrls.isEmpty()) {
            throw PredefinedErrorCode.NOT_FOUND_SERVICE.fail(null, url);
        }
        return availableServiceUrls;
    }
}
