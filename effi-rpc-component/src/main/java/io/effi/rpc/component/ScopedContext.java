package io.effi.rpc.component;

import io.effi.rpc.component.extension.ExtensionAccessor;
import io.effi.rpc.component.extension.ExtensionEntry;
import io.effi.rpc.component.extension.ExtensionLoader;
import io.effi.rpc.component.extension.ExtensionRepository;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.hook.CloseHook;
import io.effi.rpc.hook.HookExecutor;
import io.effi.rpc.hook.InitializeHook;
import io.effi.rpc.hook.StartHook;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.option.Options;
import io.effi.rpc.trait.Closeable;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.EventListener;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope;

/**
 * Manages scoped components, extensions, and lifecycle with parent context support.
 * <p>
 * Provides a hierarchical context for managing components and extensions with
 * support for initialization, startup, and shutdown lifecycle events.
 */
public abstract class ScopedContext implements ComponentAccessor, ExtensionAccessor, Closeable {

    protected final AtomicBoolean active = new AtomicBoolean(false);

    protected final AtomicBoolean closed = new AtomicBoolean(false);

    protected String name;

    protected Scope scope;

    protected ScopedContext parent;

    protected ComponentRepository componentRepository;

    protected ExtensionRepository extensionRepository;

    protected Collection<Listener<?>> listeners;

    protected Options callOptions;

    protected Options serveOptions;

    protected HierarchicalOptions options;

    protected ScopedContext(Scope scope, Class<? extends Listener<?>> listenerType,
                            ScopedContext parent, String name, ComponentRepository repository) {
        initialize(scope, listenerType, parent, repository);
        name(name);
    }

    @SuppressWarnings("unchecked")
    protected void initialize(Scope scope, Class<? extends Listener<?>> listenerType, ScopedContext parent, ComponentRepository repository) {
        this.scope = scope;
        this.parent = parent;
        this.componentRepository = checkComponentRepository(repository);
        this.extensionRepository = new ExtensionRepository(this);
        this.listeners = (Collection<Listener<?>>) extensions(listenerType);
        this.callOptions = HierarchicalOptions.create().withOwner(this).withParent(parent == null ? null : parent.callOptions());
        this.serveOptions = HierarchicalOptions.create().withOwner(this).withParent(parent == null ? null : parent.serveOptions());
        this.options = HierarchicalOptions.create().withOwner(this).withParent(parent == null ? null : parent.options());
        HookExecutor.initialize().execute(listeners, this, this::doInit);
    }

    /**
     * Renames this context and updates its parent registry.
     *
     * @param name new context name
     * @return this context
     */
    public ScopedContext name(String name) {
        String newName = AssertUtil.notBlank(name, "id");
        this.name = changeName(this.name, newName);
        return this;
    }

    private ComponentRepository checkComponentRepository(ComponentRepository repository) {
        if (repository == null) return new DelegateComponentRepository(this);
        if (repository instanceof ScopedContextOwned owned) owned.withOwner(this);
        return repository;
    }

    /**
     * Returns call-scoped options.
     */
    public Options callOptions() {
        return callOptions;
    }

    /**
     * Returns serve-scoped options.
     */
    public Options serveOptions() {
        return serveOptions;
    }

    /**
     * Returns context-level options.
     */
    public HierarchicalOptions options() {
        return options;
    }

    protected void doInit() {
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

    /**
     * Returns the component registry for this context.
     */
    public ComponentRegistry registry() {
        return componentRepository;
    }

    /**
     * Constructs the default instance of a {@link ScopedContext}.
     * <p>
     * Registers this instance early into the given {@link LazySingleton} to prevent
     * recursive creation during initialization.
     *
     * @see ScopedPlatform#defaultInstance()
     * @see ScopedApplication#defaultInstance()
     * @see ScopedModule#defaultInstance()
     */
    @SuppressWarnings("unchecked")
    protected ScopedContext(Scope scope, Class<? extends Listener<?>> listenerType,
                            ScopedContext parent, LazySingleton<? extends ScopedContext> defaultScopedContext) {
        ((LazySingleton<ScopedContext>) defaultScopedContext).expose(this);
        initialize(scope, listenerType, parent, null);
        name(Constant.DEFAULT_NAME);
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
    public <T> Collection<T> components(Class<T> type, BiPredicate<String, T> filter) {
        return componentRepository.components(type, filter);
    }

    @Override
    public <T> Map<String, T> namedComponents(Class<T> type, BiPredicate<String, T> filter) {
        return componentRepository.namedComponents(type, filter);
    }

    @Override
    public int componentCount(Class<?> type) {
        return componentRepository.componentCount(type);
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

    /**
     * Returns whether this context supports the supplied scope.
     *
     * @param scope component scope
     * @return {@code true} when the scope matches
     */
    public boolean matchesScope(Scope scope) {
        return this.scope == scope || Scope.UNIVERSAL == scope;
    }

    /**
     * Returns the context name.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the context scope.
     */
    public Scope scope() {
        return scope;
    }

    /**
     * Returns the parent context.
     */
    public ScopedContext parent() {
        return parent;
    }

    /**
     * Starts this context and notifies start listeners.
     */
    public void start() {
        if (closed.get()) {
            throw new IllegalStateException("Scoped context '" + name + "' is already closed");
        }
        if (active.compareAndSet(false, true)) {
            HookExecutor.start().execute(listeners, this, this::doStart);
        }
    }

    protected void doStart() {

    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            active.set(false);
            HookExecutor.close().execute(listeners, this, this::doClose);
            ObjectUtil.release(componentRepository);
            ObjectUtil.release(extensionRepository);
        }
    }

    @Override
    public boolean active() {
        return active.get();
    }

    protected void doClose() {

    }

    /**
     * Listens to the lifecycle of the {@link ScopedContext}.
     *
     * @see ScopedPlatform.Listener
     * @see ScopedApplication.Listener
     * @see ScopedModule.Listener
     */
    protected interface Listener<T extends ScopedContext> extends InitializeHook<T>, StartHook<T>, CloseHook<T>, EventListener {

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
