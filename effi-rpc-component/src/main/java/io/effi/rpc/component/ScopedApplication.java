package io.effi.rpc.component;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages application-level resources, modules, and lifecycle.
 * <p>
 * Provides application-scoped context for managing modules and
 * application-wide resources within a platform hierarchy.
 */
@ScopedComponent(scope = PLATFORM)
public final class ScopedApplication extends ScopedContext implements ScopedPlatform.Supplier {

    private static final AtomicInteger NUM = new AtomicInteger(0);

    private final LazySingleton<ScopedModule> defaultModule = LazySingleton.from(ScopedModule::new);

    ScopedApplication(ScopedPlatform parent, String name, ComponentRepository repository) {
        super(APPLICATION, Listener.class, parent, name, repository);
    }

    ScopedApplication(LazySingleton<ScopedApplication> defaultApplication) {
        super(APPLICATION, Listener.class, ScopedPlatform.defaultPlatform(), defaultApplication);
    }

    public static ScopedApplication defaultApplication() {
        return ScopedPlatform.defaultPlatform().defaultApplication();
    }

    @Override
    public ScopedApplication withName(String name) {
        return (ScopedApplication) super.withName(name);
    }

    @Override
    protected void doStart() {
        modules().forEach(ScopedModule::start);
    }

    @Override
    protected void doClose() {
        modules().forEach(ScopedModule::close);
    }

    @Override
    public ScopedPlatform platform() {
        return (ScopedPlatform) parent;
    }

    public ScopedModule newModule() {
        return newModule(null);
    }

    public ScopedModule newModule(String name) {
        return newModule(name, null);
    }

    public ScopedModule newModule(String name, ComponentRepository repository) {
        if (StringUtil.isBlank(name)) name = "module-" + NUM.incrementAndGet();
        return new ScopedModule(this, name, repository);
    }

    public ScopedModule lookupModule(String name) {
        return namedComponent(ScopedModule.class, name);
    }

    public ScopedModule defaultModule() {
        return defaultModule.ensure();
    }

    public Collection<ScopedModule> modules() {
        return components(ScopedModule.class);
    }

    /**
     * Listens to the lifecycle of the {@link ScopedApplication}.
     */
    @Extensible(lazyLoad = false, scope = APPLICATION)
    public interface Listener extends ScopedContext.Listener<ScopedApplication> {}

    /**
     * Accepts the {@link ScopedApplication} context.
     * <p>
     * Only for application-level extensions.
     */
    public interface Acceptor extends ScopedContext.Acceptor<ScopedApplication> {}

    /**
     * Supplies access to the {@link ScopedApplication}.
     */
    public interface Supplier extends ScopedPlatform.Supplier {

        @Override
        default ScopedPlatform platform() {
            return application().platform();
        }

        /**
         * Returns the associated {@link ScopedApplication}.
         */
        ScopedApplication application();

    }

    /**
     * Holds a reference to an {@link ScopedApplication}.
     */
    public abstract static class Holder implements Supplier {

        protected ScopedApplication application;

        public Holder(ScopedApplication application) {
            this.application = AssertUtil.notNull(application, "application");
        }

        @Override
        public ScopedApplication application() {
            return application;
        }
    }
}
