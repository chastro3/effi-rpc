package io.effi.rpc.governance.router;

import io.effi.rpc.config.RouterConfig;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.util.StringUtil;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides the default implementation of {@link Router}.
 */
@Extension(Component.DEFAULT)
public class DefaultRouter implements Router {

    @Override
    public List<URL> route(InvocationContext<?, Caller<?>> context, List<URL> urls) {
        URL url = context.envelope().url();
        // filter by group
        String group = url.getParam(KeyConstant.GROUP);
        if (!StringUtil.isBlank(group)) {
            urls = urls.stream().filter(item -> Objects.equals(item.getParam(KeyConstant.GROUP), group)).collect(Collectors.toList());
        }
        // filter by router rule
        EffiRpcModule module = context.module();
        Collection<RouterConfig> routerConfigs = Collections.emptyList();
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
