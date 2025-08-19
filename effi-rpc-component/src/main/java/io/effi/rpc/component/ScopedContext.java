package io.effi.rpc.component;

import io.effi.rpc.component.extension.ExtensionAccessor;
import io.effi.rpc.component.extension.ExtensionEntry;
import io.effi.rpc.component.extension.ExtensionLoader;
import io.effi.rpc.component.extension.ExtensionRepository;
import io.effi.rpc.config.ConfigValues;
import io.effi.rpc.config.DefaultHierarchicalConfig;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.hook.CloseHook;
import io.effi.rpc.util.hook.HookExecutor;
import io.effi.rpc.util.hook.InitializeHook;
import io.effi.rpc.util.hook.StartHook;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.Collection;
import java.util.EventListener;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope;

/**
 * Manages scoped components, extensions, and lifecycle with parent context support.
 * <p>
 * Provides a hierarchical context for managing components and extensions with
 * support for initialization, startup, and shutdown lifecycle events.
 */
public abstract class ScopedContext implements ComponentAccessor, ExtensionAccessor, Closeable {

    protected final AtomicBoolean active = new AtomicBoolean(false);

    protected String name;

    protected Scope scope;

    protected ScopedContext parent;

    protected ComponentRepository componentRepository;

    protected ExtensionRepository extensionRepository;

    protected Collection<Listener<?>> listeners;

    protected HierarchicalConfig callerConfig;

    protected HierarchicalConfig calleeConfig;

    protected ScopedContext(Scope scope, Class<? extends Listener<?>> listenerType,
                            ScopedContext parent, String name, ComponentRepository repository) {
        initialize(scope, listenerType, parent, repository);
        withName(name);
    }

    /**
     * Constructs the default instance of a {@link ScopedContext}.
     * <p>
     * Registers this instance early into the given {@link LazySingleton} to prevent
     * recursive creation during initialization.
     *
     * @see ScopedPlatform#defaultPlatform()
     * @see ScopedApplication#defaultApplication()
     * @see ScopedApplication#defaultModule()
     */
    @SuppressWarnings("unchecked")
    protected ScopedContext(Scope scope, Class<? extends Listener<?>> listenerType,
                            ScopedContext parent, LazySingleton<? extends ScopedContext> defaultScopedContext) {
        ((LazySingleton<ScopedContext>) defaultScopedContext).expose(this);
        initialize(scope, listenerType, parent, null);
        withName(ConfigValues.DEFAULT);
    }

    @Override
    public <T> T singleComponent(Class<T> type) {
        return componentRepository.singleComponent(type);
    }

    @Override
    public <T> T namedComponent(Class<T> type, String name) {
        return componentRepository.namedComponent(type, name);
    }

    @Override
    public <T> T externalComponent(GenericKey<T> name) {
        return componentRepository.externalComponent(name);
    }

    @Override
    public int componentCount(Class<?> type) {
        return componentRepository.componentCount(type);
    }

    @Override
    public <T> Collection<T> components(Class<T> type, BiPredicate<String, T> filter) {
        return componentRepository.components(type, filter);
    }

    @Override
    public <T> Map<String, T> namedComponents(Class<T> type, BiPredicate<String, T> filter) {
        return componentRepository.namedComponents(type, filter);
    }

    @Override
    public <T> ExtensionLoader<T> extensionLoader(Class<T> type) {
        return extensionRepository.extensionLoader(type);
    }

    @Override
    public <T> T preferredExtension(Class<T> type, String name) {
        return extensionRepository.preferredExtension(type, name);
    }

    @Override
    public <T> T namedExtension(Class<T> type, String name) {
        return extensionRepository.namedExtension(type, name);
    }

    @Override
    public <T> T adaptiveExtension(Class<T> type, Function<String, String> nameGetter) {
        return extensionRepository.adaptiveExtension(type, nameGetter);
    }

    @Override
    public <T> T primaryExtension(Class<T> type) {
        return extensionRepository.primaryExtension(type);
    }

    @Override
    public <T> Collection<T> extensions(Class<T> type, BiPredicate<String, ExtensionEntry<T>> filter) {
        return extensionRepository.extensions(type, filter);
    }

    @Override
    public <T> Map<String, T> namedExtensions(Class<T> type, BiPredicate<String, ExtensionEntry<T>> filter) {
        return extensionRepository.namedExtensions(type, filter);
    }

    public boolean matchesScope(Scope scope) {
        return this.scope == scope || Scope.UNIVERSAL == scope;
    }

    public ScopedContext withName(String name) {
        String newName = AssertUtil.notBlank(name, "name");
        this.name = changeName(this.name, newName);
        return this;
    }

    public ComponentRegistry registry() {
        return componentRepository;
    }

    public String name() {
        return name;
    }

    public Scope scope() {
        return scope;
    }

    public ScopedContext parent() {
        return parent;
    }

    public HierarchicalConfig callerConfig() {
        return callerConfig;
    }

    public HierarchicalConfig calleeConfig() {
        return calleeConfig;
    }

    public void start() {
        if (active.compareAndSet(false, true)) {
            HookExecutor.start().execute(listeners, this, this::doStart);
        }
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public void close() {
        if (active.compareAndSet(true, false)) {
            HookExecutor.close().execute(listeners, this, this::doClose);
            ObjectUtil.release(componentRepository);
            ObjectUtil.release(extensionRepository);
        }
    }

    @SuppressWarnings("unchecked")
    protected void initialize(Scope scope, Class<? extends Listener<?>> listenerType,
                              ScopedContext parent, ComponentRepository repository) {
        this.scope = scope;
        this.parent = parent;
        this.componentRepository = checkComponentRepository(repository);
        this.extensionRepository = new ExtensionRepository(this);
        this.listeners = (Collection<Listener<?>>) extensions(listenerType);
        this.callerConfig = new DefaultHierarchicalConfig(this, parent == null ? null : parent.callerConfig());
        this.calleeConfig = new DefaultHierarchicalConfig(this, parent == null ? null : parent.calleeConfig());
        HookExecutor.initialize().execute(listeners, this, this::doInit);
    }

    @SuppressWarnings("unchecked")
    protected String changeName(String oldName, String newName) {
        if (parent != null && !Objects.equals(oldName, newName)) {
            Class<ScopedContext> type = (Class<ScopedContext>) this.getClass();
            if (StringUtil.isNotBlank(oldName))
                parent.registry().remove(type, oldName);
            parent.registry().register(type, newName, this);
        }
        return newName;
    }

    private ComponentRepository checkComponentRepository(ComponentRepository repository) {
        if (repository == null) return new DelegateComponentRepository(this);
        if (repository instanceof ScopedContextOwned owned) owned.withOwner(this);
        return repository;
    }

    protected abstract void doStart();

    protected abstract void doClose();

    protected void doInit() {
    }

    /**
     * Listens to the lifecycle of the {@link ScopedContext}.
     *
     * @see ScopedPlatform.Listener
     * @see ScopedApplication.Listener
     * @see ScopedModule.Listener
     */
    protected interface Listener<T extends ScopedContext>
            extends InitializeHook<T>, StartHook<T>, CloseHook<T>, EventListener {

    }

    /**
     * Accepts a {@link ScopedContext} when an extension is loaded for the first time,
     * allowing it to interact with its associated scoped context.
     *
     * @see ScopedPlatform.Acceptor
     * @see ScopedApplication.Acceptor
     * @see ScopedModule.Acceptor
     */
    protected interface Acceptor<T extends ScopedContext> {

        /**
         * Accepts the associated {@link ScopedContext} when the extension is first loaded.
         *
         * @param scopedContext the scoped context bound to this extension
         */
        void accept(T scopedContext);
    }
}
