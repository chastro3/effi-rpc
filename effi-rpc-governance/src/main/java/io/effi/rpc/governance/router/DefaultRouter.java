package io.effi.rpc.governance.router;

import io.effi.rpc.common.constant.Component;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.config.RouterConfig;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link Router}.
 */
@Extension(Component.DEFAULT)
public class DefaultRouter implements Router {

    @Override
    public List<URL> route(InvocationContext<?, Caller<?>> context, List<URL> urls) {
        URL url = context.source().url();
        // filter by group
        String group = url.getParam(KeyConstant.GROUP);
        if (!StringUtil.isBlank(group)) {
            urls = urls.stream().filter(item -> Objects.equals(item.getParam(KeyConstant.GROUP), group)).collect(Collectors.toList());
        }
        // filter by router rule
        EffiRpcModule module = context.module();
        Collection<RouterConfig> routerConfigs = module.routerConfigManager().values();
        LinkedList<URL> result = new LinkedList<>();
        boolean hadConfig = false;
        for (RouterConfig routerConfig : routerConfigs) {
            Pattern urlPattern = Pattern.compile(routerConfig.urlRegex());
            if (urlPattern.matcher(url.toString()).find()) {
                hadConfig = true;
                for (URL serviceUrl : urls) {
                    Pattern targetPattern = Pattern.compile(routerConfig.matchTargetRegex());
                    if (targetPattern.matcher(serviceUrl.toString()).find()) {
                        result.add(serviceUrl);
                    }
                }
            }
        }
        return hadConfig ? result : urls;
    }
}
