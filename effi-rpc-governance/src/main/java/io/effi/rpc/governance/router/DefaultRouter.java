package io.effi.rpc.governance.router;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.config.RouterConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.effi.rpc.config.ConfigValues.DEFAULT;

/**
 * Provides the default implementation of {@link Router}.
 */
@Extension(DEFAULT)
public class DefaultRouter implements Router {

    @Override
    public List<ServiceInstance> route(CallContext<?, Caller<?>> context, List<ServiceInstance> instances) {
        SmartURL smartUrl = context.message().url();
        Caller<?> caller = context.peer();
        // filter by group
        String group = caller.getConfig(KeyConstant.GROUP);
        if (!StringUtil.isBlank(group)) {
            instances = instances.stream().filter(item -> Objects.equals(caller.getConfig(KeyConstant.GROUP), group)).collect(Collectors.toList());
        }
        // filter by router rule
        ScopedModule module = context.module();
        Collection<RouterConfig> routerConfigs = Collections.emptyList();
        LinkedList<ServiceInstance> result = new LinkedList<>();
        boolean hadConfig = false;
        for (RouterConfig routerConfig : routerConfigs) {
            Pattern urlPattern = Pattern.compile(routerConfig.urlRegex());
            if (urlPattern.matcher(smartUrl.toString()).find()) {
                hadConfig = true;
                for (ServiceInstance instance : instances) {
                    Pattern targetPattern = Pattern.compile(routerConfig.matchTargetRegex());
                    if (targetPattern.matcher(instance.toString()).find()) {
                        result.add(instance);
                    }
                }
            }
        }
        return hadConfig ? result : instances;
    }
}
