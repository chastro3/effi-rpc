package io.effi.rpc.governance.router;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.registry.ServiceInstance;

import java.util.List;

import static io.effi.rpc.governance.router.DefaultRouter.NAME;

/**
 * Provides the default implementation of {@link Router}.
 */
@Extension(value = NAME, primary = true)
public class DefaultRouter implements Router {

    public static final String NAME = Constant.DEFAULT_NAME;

    @Override
    public List<ServiceInstance> route(CallContext<?, Caller<?>> context, List<ServiceInstance> instances) {
        RouterConfig routerConfig = context.module().singleComponent(RouterConfig.class);
        if (routerConfig == null || routerConfig.rules().isEmpty()) {
            return instances;
        }
        String url = context.message().url().toString();
        for (RouterConfig.Rule rule : routerConfig.rules()) {
            if (rule.matchesUrl(url)) {
                return instances.stream()
                        .filter(rule::matchesInstance)
                        .toList();
            }
        }
        return instances;
    }
}
