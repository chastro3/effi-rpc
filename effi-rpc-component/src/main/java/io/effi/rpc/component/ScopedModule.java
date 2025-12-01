package io.effi.rpc.component;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.LazySingleton;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Manages module-level resources and lifecycle.
 * <p>
 * Provides module-scoped context for managing component resources
 * within an application hierarchy.
 */
@ScopedComponent(scope = APPLICATION)
public final class ScopedModule extends ScopedContext implements ScopedApplication.Supplier {

    ScopedModule(ScopedApplication parent, String name, ComponentRepository repository) {
        super(MODULE, Listener.class, parent, name, repository);
    }

    ScopedModule(LazySingleton<ScopedModule> defaultModule) {
        super(MODULE, Listener.class, ScopedApplication.defaultInstance(), defaultModule);
    }

    public static ScopedModule defaultInstance() {
        return ScopedApplication.defaultInstance().defaultModule();
    }

    @Override
    public ScopedModule name(String name) {
        return (ScopedModule) super.name(name);
    }

    @Override
    public ScopedApplication application() {
        return (ScopedApplication) parent();
    }

    /**
     * Listens to the lifecycle of the {@link ScopedModule}.
     */
    @Extensible(lazyLoad = false, scope = MODULE)
    public interface Listener extends ScopedContext.Listener<ScopedModule> {}

    /**
     * Accepts the {@link ScopedModule} context.
     * <p>
     * Only for module-level extensions.
     */
    public interface Acceptor extends ScopedContext.Acceptor<ScopedModule> {}

    /**
     * Provides access to the {@link ScopedModule}.
     */
    public interface Supplier extends ScopedApplication.Supplier {

        @Override
        default ScopedApplication application() {
            return module().application();
        }

        /**
         * Returns the associated {@link ScopedModule}.
         */
        ScopedModule module();
    }

    /**
     * Holds a reference to an {@link ScopedModule}.
     */
    public abstract static class Holder implements Supplier {

        protected ScopedModule module;

        public Holder(ScopedModule module) {
            this.module = AssertUtil.notNull(module, "module");
        }

        @Override
        public ScopedModule module() {
            return module;
        }
    }
}
