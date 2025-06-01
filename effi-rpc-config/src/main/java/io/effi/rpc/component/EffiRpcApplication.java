package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.FlatConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages application-level resources and lifecycle.
 */
@ScopedComponent(scope = PLATFORM)
public final class EffiRpcApplication extends AbstractScopedComponentRepository implements PlatformSource {

    private static final AtomicInteger NUM = new AtomicInteger(0);

    private static final Object LOCK = new Object();

    private final Config providerConfig = new FlatConfig(this);

    private final Config consumerConfig = new FlatConfig(this);

    private volatile EffiRpcModule defaultModule;

    EffiRpcApplication(String name, EffiRpcPlatform parent) {
        name(name);
        initialize(APPLICATION, parent, ApplicationConfiguration.class);
    }

    @Override
    protected void doStart() {
        modules().forEach(EffiRpcModule::start);
    }

    @Override
    protected void doStop() {
        modules().forEach(EffiRpcModule::stop);
    }

    @Override
    public EffiRpcPlatform platform() {
        return (EffiRpcPlatform) parent();
    }

    public EffiRpcModule newModule() {
        return newModule(null);
    }

    public EffiRpcModule newModule(String name) {
        if (StringUtil.isBlank(name)) {
            name = "module-" + NUM.incrementAndGet();
        }
        EffiRpcModule module = new EffiRpcModule(name, this);
        register(EffiRpcModule.class, name, module);
        return module;
    }

    public EffiRpcModule getModule(String name) {
        return lookup(EffiRpcModule.class, name);
    }

    public Config providerConfig() {
        return providerConfig;
    }

    public Config consumerConfig() {
        return consumerConfig;
    }

    public EffiRpcModule defaultModule() {
        if (defaultModule == null) {
            synchronized (LOCK) {
                if (defaultModule == null) {
                    defaultModule = newModule(Component.DEFAULT);
                }
            }
        }
        return defaultModule;
    }

    public Collection<EffiRpcModule> modules() {
        MultiComponent<EffiRpcModule> repository = getMultiComponent(EffiRpcModule.class);
        if (repository == null) {
            return Collections.emptyList();
        }
        return repository.components().stream().map(SingleComponent::component).toList();
    }
}
