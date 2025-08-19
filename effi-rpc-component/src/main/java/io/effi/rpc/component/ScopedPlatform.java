package io.effi.rpc.component;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages platform-level resources, applications, and lifecycle.
 * <p>
 * Provides the top-level scoped context for managing applications and
 * platform-wide resources with support for default and named platforms.
 */
public final class ScopedPlatform extends ScopedContext {

    private static final Map<String, ScopedPlatform> PLATFORMS = new ConcurrentHashMap<>();

    private static final AtomicInteger NUM = new AtomicInteger(0);

    private static final LazySingleton<ScopedPlatform> DEFAULT_PLATFORM = LazySingleton.from(ScopedPlatform::new);

    private final LazySingleton<ScopedApplication> defaultApplication = LazySingleton.from(ScopedApplication::new);

    public ScopedPlatform(String name) {
        this(name, null);
    }

    public ScopedPlatform(String name, ComponentRepository repository) {
        super(PLATFORM, Listener.class, null, name, repository);
    }

    private ScopedPlatform(LazySingleton<ScopedPlatform> defaultPlatform) {
        super(PLATFORM, Listener.class, null, defaultPlatform);
    }

    public static ScopedPlatform defaultPlatform() {
        return DEFAULT_PLATFORM.ensure();
    }

    public static ScopedPlatform lookup(String name) {
        if (StringUtil.isBlank(name)) return null;
        return PLATFORMS.get(name);
    }

    @Override
    public ScopedPlatform withName(String name) {
        return (ScopedPlatform) super.withName(name);
    }

    public ScopedApplication defaultApplication() {
        return defaultApplication.ensure();
    }

    public ScopedApplication newApplication() {
        return newApplication(null);
    }

    public ScopedApplication newApplication(String name) {
        return newApplication(name, null);
    }

    public ScopedApplication newApplication(String name, ComponentRepository repository) {
        if (StringUtil.isBlank(name)) name = "application-" + NUM.incrementAndGet();
        return new ScopedApplication(this, name, repository);
    }

    public ScopedApplication lookupApplication(String name) {
        if (StringUtil.isBlank(name)) return null;
        return namedComponent(ScopedApplication.class, name);
    }

    public Collection<ScopedApplication> applications() {
        return components(ScopedApplication.class);
    }

    @Override
    protected String changeName(String oldName, String newName) {
        if (!Objects.equals(oldName, newName)) {
            if (StringUtil.isNotBlank(oldName))
                PLATFORMS.remove(oldName);
            PLATFORMS.put(newName, this);
        }
        return newName;
    }

    @Override
    protected void doStart() {
        applications().forEach(ScopedApplication::start);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    protected void doClose() {
        applications().forEach(ScopedApplication::close);
    }

    /**
     * Listens to the lifecycle of the {@link ScopedPlatform}.
     */
    @Extensible(lazyLoad = false, scope = PLATFORM)
    public interface Listener extends ScopedContext.Listener<ScopedPlatform> {}

    /**
     * Accepts the {@link ScopedPlatform} context.
     * <p>
     * Only for platform-level extensions.
     */
    public interface Acceptor extends ScopedContext.Acceptor<ScopedPlatform> {}

    /**
     * Supplies access to the {@link ScopedPlatform}.
     */
    public interface Supplier {

        /**
         * Returns the associated {@link ScopedPlatform}.
         */
        ScopedPlatform platform();
    }

    /**
     * Holds a reference to an {@link ScopedPlatform}.
     */
    public abstract static class Holder implements Supplier {

        protected ScopedPlatform platform;

        public Holder(ScopedPlatform platform) {
            this.platform = AssertUtil.notNull(platform, "platform");
        }

        @Override
        public ScopedPlatform platform() {
            return platform;
        }
    }

}
