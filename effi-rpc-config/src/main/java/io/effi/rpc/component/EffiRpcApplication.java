package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.FlatConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages application-level resources and lifecycle.
 */
@ScopedComponent(scope = PLATFORM)
public final class EffiRpcApplication extends ScopedContext implements EffiRpcPlatform.Provider {

    private static final AtomicInteger NUM = new AtomicInteger(0);

    private static final Object LOCK = new Object();

    private final Config providerConfig = new FlatConfig(this);

    private final Config consumerConfig = new FlatConfig(this);

    private volatile EffiRpcModule defaultModule;

    EffiRpcApplication(String name, EffiRpcPlatform parent, ComponentStore repository) {
        name(name);
        initialize(APPLICATION, parent, repository, ApplicationConfiguration.class);
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
        return newModule(name, null);
    }

    public EffiRpcModule newModule(String name, ComponentStore repository) {
        if (StringUtil.isBlank(name)) {
            name = "module-" + NUM.incrementAndGet();
        }
        EffiRpcModule module = new EffiRpcModule(name, this, repository);
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

    public EffiRpcApplication setDefaultModule(EffiRpcModule module) {
        this.defaultModule = module;
        return this;
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
        return listOf(EffiRpcModule.class);
    }

    /**
     * Provides access to the {@link EffiRpcApplication}.
     */
    public interface Provider extends EffiRpcPlatform.Provider {

        @Override
        default EffiRpcPlatform platform() {
            return application().platform();
        }

        /**
         * Returns the associated {@link EffiRpcApplication}.
         */
        EffiRpcApplication application();

    }

    /**
     * Holds a reference to an {@link EffiRpcApplication}.
     */
    public abstract static class Holder implements Provider {

        protected EffiRpcApplication application;

        public Holder(EffiRpcApplication application) {
            this.application = application;
        }

        @Override
        public EffiRpcApplication application() {
            return application;
        }
    }
}
