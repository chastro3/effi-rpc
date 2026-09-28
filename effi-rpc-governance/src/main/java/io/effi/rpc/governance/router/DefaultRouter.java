package io.effi.rpc.governance.router;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.RouterConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.StringUtil;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static io.effi.rpc.governance.router.DefaultRouter.NAME;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.GovernanceOptions;

/**
 * Provides the default implementation of {@link Router}.
 */
@Extension(value = NAME, primary = true)
public class DefaultRouter implements Router {

    public static final String NAME = Constant.DEFAULT_NAME;

    @Override
    public List<ServiceInstance> route(CallContext<?, Caller<?>> context, List<ServiceInstance> instances) {
        SmartURL smartUrl = context.message().url();
        Caller<?> caller = context.peer();
        List<ServiceInstance> candidates = instances;
        String group = caller.option(GovernanceOptions.GROUP);
        if (!StringUtil.isBlank(group)) {
            candidates = instances.stream()
                    .filter(instance -> Objects.equals(group, instance.metadata().get(KeyConstant.GROUP)))
                    .toList();
        }
        RouterConfig routerConfig = context.module().singleComponent(RouterConfig.class);
        if (routerConfig == null || !Pattern.compile(routerConfig.urlRegex()).matcher(smartUrl.toString()).find()) {
            return candidates;
        }
        String targetRegex = StringUtil.isBlank(routerConfig.matchTargetRegex())
                ? ".*"
                : routerConfig.matchTargetRegex();
        Pattern targetPattern = Pattern.compile(targetRegex);
        return candidates.stream()
                .filter(instance -> targetPattern.matcher(instance.toString()).find())
                .toList();
    }
}
