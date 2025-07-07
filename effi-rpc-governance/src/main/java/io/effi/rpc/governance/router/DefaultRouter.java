package io.effi.rpc.governance.router;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.RouterConfig;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides the default implementation of {@link Router}.
 */
@Extension(Component.DEFAULT)
public class DefaultRouter implements Router {

    @Override
    public List<URL> route(CallContext<?, Caller<?>> context, List<URL> urls) {
        URL url = context.message().url();
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
