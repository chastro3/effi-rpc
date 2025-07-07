package io.effi.rpc.component;

import io.effi.rpc.constant.Constant;
import io.effi.rpc.lifecycle.Lifecycle;
import io.effi.rpc.lifecycle.LifecycleConfiguration;
import io.effi.rpc.lifecycle.LifecyclePhase;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.resoruce.Cleanable;
import io.effi.rpc.util.resoruce.Closeable;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope;

/**
 * Manages scoped components, extensions, and lifecycle with parent context support.
 */
public abstract class ScopedContext implements ComponentStore, ExtensionAccessor, Lifecycle {

    private static final Map<Class<?>, ScopedComponentDescriptor> COMPONENT_DESCRIPTORS = loadComponentDescriptors();

    protected final Object lock = new Object();

    protected volatile State state;

    protected String name;

    protected Scope scope;

    protected ScopedContext parent;

    protected ComponentStore componentStore;

    protected ExtensionStore extensionStore;

    protected Collection<LifecycleConfiguration<ScopedContext>> configurations;

    public static List<Class<?>> findMatchingScopedComponentTypes(Class<?> type) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> defType : COMPONENT_DESCRIPTORS.keySet()) {
            if (defType.isAssignableFrom(type)) {
                result.add(defType);
            }
        }
        return result.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(result);
    }

    public static ScopedComponentDescriptor ensureScopedComponentDescriptor(Class<?> type, ScopedContext repository) {
        ScopedComponentDescriptor descriptor = getScopedComponentDescriptor(type);
        AssertUtil.condition(
                descriptor != null,
                "Component '{}' is not annotated with @ScopedComponent or not registered",
                type.getName()
        );
        AssertUtil.condition(
                repository.matchesScope(descriptor.scope()),
                "Component '{}' is not supported in '{}'.Supported scope: [{} or UNIVERSAL]",
                type.getName(), ObjectUtil.simpleClassName(repository), repository.scope().name()
        );
        return descriptor;
    }

    public static ScopedComponentDescriptor getScopedComponentDescriptor(Class<?> type) {
        return COMPONENT_DESCRIPTORS.get(type);
    }

    public boolean matchesScope(Scope scope) {
        return this.scope == scope || Scope.UNIVERSAL == scope;
    }

    public Scope scope() {
        return scope;
    }

    private static Map<Class<?>, ScopedComponentDescriptor> loadComponentDescriptors() {
        HashMap<Class<?>, ScopedComponentDescriptor> map = new HashMap<>();
        try {
            ClassLoader classLoader = ClassUtil.getClassLoader(ScopedContext.class);
            Properties properties = new Properties();
            Enumeration<URL> resources = classLoader.getResources(Constant.COMPONENT_PROPERTIES_PATH);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream in = url.openStream()) {
                    Properties p = new Properties();
                    p.load(in);
                    properties.putAll(p);
                }
            }
            for (String type : properties.stringPropertyNames()) {
                Class<?> componentType = classLoader.loadClass(type);
                String value = properties.getProperty(type);
                String[] split = value.split(",");
                Scope componentScope = Scope.valueOf(split[0]);
                Kind componentKind = Kind.valueOf(split[1]);
                map.put(componentType, ScopedComponentDescriptor.valueOf(componentScope, componentKind));
            }
        } catch (Exception e) {
            throw new IllegalStateException(Messages.parseFile(Constant.COMPONENT_PROPERTIES_PATH), e);
        }
        return map;
    }

    public ScopedContext name(String name) {
        this.name = AssertUtil.notBlank(name, "name");
        return this;
    }

    @Override
    public <T> ComponentStore register(Class<T> type, T component) {
        componentStore.register(type, component);
        return this;
    }

    @Override
    public <T> ComponentStore register(Class<T> type, String name, T component) {
        componentStore.register(type, name, component);
        return this;
    }

    @Override
    public <T> T lookup(Class<T> type) {
        return componentStore.lookup(type);
    }

    @Override
    public <T> T lookup(Class<T> type, String name) {
        return componentStore.lookup(type, name);
    }

    @Override
    public <T> T lookupWrapped(GenericKey<T> name) {
        return componentStore.lookupWrapped(name);
    }

    @Override
    public int sizeOf(Class<?> type) {
        return componentStore.sizeOf(type);
    }

    @Override
    public <T> Collection<T> listOf(Class<T> type, BiPredicate<String, T> filter) {
        return componentStore.listOf(type, filter);
    }

    @Override
    public <T> Map<String, T> mapOf(Class<T> type, BiPredicate<String, T> filter) {
        return componentStore.mapOf(type, filter);
    }

    @Override
    public ComponentStore remove(Class<?> type) {
        componentStore.remove(type);
        return this;
    }

    @Override
    public ComponentStore remove(Class<?> type, String name) {
        componentStore.remove(type, name);
        return this;
    }

    @Override
    public <T> ExtensionLoader<T> getLoader(Class<T> type) {
        return extensionStore.getLoader(type);
    }

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        return extensionStore.getExtension(type, name);
    }

    @Override
    public <T> T getAdaptiveExtension(Class<T> type, Function<String, String> nameGetter) {
        return extensionStore.getAdaptiveExtension(type, nameGetter);
    }

    @Override
    public <T> T getDefaultExtension(Class<T> type) {
        return extensionStore.getDefaultExtension(type);
    }

    @Override
    public <T> Collection<T> extensionsOf(Class<T> type, BiPredicate<String, ExtensionHolder<T>> filter) {
        return extensionStore.extensionsOf(type, filter);
    }

    @Override
    public <T> Map<String, T> extensionMapOf(Class<T> type, BiPredicate<String, ExtensionHolder<T>> filter) {
        return extensionStore.extensionMapOf(type, filter);
    }

    public String name() {
        return name;
    }

    public ScopedContext parent() {
        return parent;
    }

    public void tryClearResource(Object component) {
        if (component instanceof Closeable closeable && closeable.isActive()) {
            closeable.close();
        } else if (component instanceof Cleanable cleanable) {
            cleanable.clear();
        }
    }

    @SuppressWarnings("unchecked")
    protected void initialize(Scope scope, ScopedContext parent, ComponentStore componentStore, Class<?> configurationType) {
        this.scope = AssertUtil.notNull(scope, "scope");
        AssertUtil.notNull(configurationType, "configuration type");
        this.parent = parent;
        this.componentStore = checkComponentRepository(componentStore);
        this.extensionStore = new ExtensionStore(this);
        this.configurations = (Collection<LifecycleConfiguration<ScopedContext>>) extensionsOf(configurationType);
        init();
    }

    private ComponentStore checkComponentRepository(ComponentStore repository) {
        if (repository == null) {
            return new DelegateComponentStore(this);
        }
        if (repository instanceof ScopedContextOwned owned) {
            owned.setOwner(this);
        }
        return repository;
    }

    @Override
    public void init() {
        executeLifecyclePhase(LifecyclePhase.INIT, this::doInit);
    }

    @Override
    public void start() {
        executeLifecyclePhase(LifecyclePhase.START, this::doStart);
    }

    @Override
    public void stop() {
        executeLifecyclePhase(LifecyclePhase.STOP, this::doStop);
        clear();
    }

    protected abstract void doStop();

    @Override
    public void clear() {
        extensionStore.clear();
        componentStore.clear();
    }

    private void executeLifecyclePhase(LifecyclePhase phase, Runnable action) {
        if (phase.isAllowed(state)) {
            synchronized (lock) {
                if (phase.isAllowed(state)) {
                    phase.execute(configurations, this, action);
                    this.state = phase.next();
                }
            }
        }
    }

    protected abstract void doStart();

    protected void doInit() {
    }
}
