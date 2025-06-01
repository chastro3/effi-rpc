package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.lifecycle.LifecycleConfiguration;
import io.effi.rpc.lifecycle.LifecyclePhase;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope;

/**
 * Provides an abstract implementation of {@link ScopedComponentRepository}.
 */
public abstract class AbstractScopedComponentRepository implements ScopedComponentRepository {

    private static final Map<Class<?>, ScopedComponentDefinition> COMPONENT_DEFINITIONS = loadComponentDefinitions();

    protected final Object lock = new Object();

    protected Scope scope;

    protected AbstractScopedComponentRepository parent;

    protected String name;

    protected List<LifecycleConfiguration<ScopedComponentRepository>> configurations;

    protected Map<Class<?>, SingleComponent<?>> singleComponents = new ConcurrentHashMap<>();

    protected Map<Class<?>, MultiComponent<?>> multiComponents = new ConcurrentHashMap<>();

    protected volatile State state;

    @SuppressWarnings("unchecked")
    protected void initialize(Scope scope, AbstractScopedComponentRepository parent, Class<?> configurationType) {
        AssertUtil.notNull(configurationType, "configuration type");
        this.configurations = (List<LifecycleConfiguration<ScopedComponentRepository>>)
                ExtensionLoader.loadExtensions(configurationType);
        this.scope = AssertUtil.notNull(scope, "scope");
        this.parent = parent;
        init();
    }

    @Override
    public void clear() {
        stop();
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
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Scope scope() {
        return scope;
    }

    @Override
    public ScopedComponentRepository parent() {
        return parent;
    }

    @Override
    public <T> ScopedComponentRepository register(Class<T> type, T component, String... tags) {
        String key = null;
        if (component instanceof Identifiable identifiable) {
            key = identifiable.id();
        }
        return register(type, key, component, tags);
    }

    @Override
    public <T> ScopedComponentRepository register(Class<T> type, String key, T component, String... tags) {
        ScopedComponentDefinition componentDef = getComponentDefinition(type);
        return register(type, key, component, componentDef, tags);
    }

    @Override
    public <T> T lookup(Class<T> type) {
        return lookup(type, null);
    }

    @Override
    public <T> T lookup(Class<T> type, String key) {
        ScopedComponentDefinition componentDef = getComponentDefinition(type);
        return lookup(key, type, componentDef);
    }

    @Override
    public int sizeOf(Class<?> type) {
        ScopedComponentDefinition componentDef = getComponentDefinition(type);
        Kind kind = componentDef.kind();
        if (kind == Kind.SINGLE) {
            SingleComponent<?> singleComponent = getSingleComponent(type);
            if (singleComponent != null)
                return 1;
        } else if (kind == Kind.MULTI) {
            MultiComponent<?> multiComponent = getMultiComponent(type);
            if (multiComponent != null) {
                return multiComponent.size();
            }
        }
        return 0;
    }

    @Override
    public <T> Collection<T> listOf(Class<T> type, String... tags) {
        ScopedComponentDefinition componentDef = getComponentDefinition(type);
        Kind kind = componentDef.kind();
        if (kind == Kind.SINGLE) {
            SingleComponent<T> singleComponent = getSingleComponent(type);
            if (singleComponent != null)
                return Collections.singletonList(singleComponent.component());
        } else if (kind == Kind.MULTI) {
            MultiComponent<T> multiComponent = getMultiComponent(type);
            if (multiComponent != null) {
                return multiComponent.listValueOf(tags);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public <T> Map<String, T> mapOf(Class<T> type, String... tags) {
        ScopedComponentDefinition componentDef = getComponentDefinition(type);
        Kind kind = componentDef.kind();
        if (kind == Kind.SINGLE) {
            SingleComponent<T> singleComponent = getSingleComponent(type);
            if (singleComponent != null)
                return Collections.singletonMap(singleComponent.key(), singleComponent.component());
        } else if (kind == Kind.MULTI) {
            MultiComponent<T> multiComponent = getMultiComponent(type);
            if (multiComponent != null) return multiComponent.mapValueOf(tags);
        }
        return Collections.emptyMap();
    }

    @Override
    public ScopedComponentRepository remove(Class<?> type) {
        return remove(type, null);
    }

    @Override
    public ScopedComponentRepository remove(Class<?> type, String key) {
        ScopedComponentDefinition componentDef = getComponentDefinition(type);
        return remove(type, key, componentDef);
    }

    protected void doInit() {
    }

    protected abstract void doStart();

    protected abstract void doStop();

    public ScopedComponentRepository name(String name) {
        this.name = AssertUtil.notBlank(name, "name");
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> SingleComponent<T> getSingleComponent(Class<T> type) {
        return (SingleComponent<T>) singleComponents.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T> MultiComponent<T> getMultiComponent(Class<T> type) {
        return (MultiComponent<T>) multiComponents.get(type);
    }

    protected static void registerScopedComponentDefinition(Class<?> type, Scope scope, Kind kind) {
        COMPONENT_DEFINITIONS.putIfAbsent(type, ScopedComponentDefinition.valueOf(scope, kind));
    }

    private ScopedComponentDefinition getComponentDefinition(Class<?> type) {
        ScopedComponentDefinition componentDef = COMPONENT_DEFINITIONS.get(type);
        if (componentDef == null) {
            throw new IllegalArgumentException("Component type [" + type.getName() + "] is not annotated with @ScopedComponent or not registered");
        }
        return componentDef;
    }

    @SuppressWarnings("unchecked")
    private <T> ScopedComponentRepository register(Class<T> type, String key, T component, ScopedComponentDefinition componentDef, String... tags) {
        if (matched(componentDef.scope())) {
            Kind kind = componentDef.kind();
            String registeredKey = StringUtil.isBlank(key)
                    ? ObjectUtil.simpleClassName(kind == Kind.SINGLE ? type : component)
                    : key;
            if (kind == Kind.SINGLE) {
                SingleComponent<T> singleComponent = (SingleComponent<T>) singleComponents
                        .computeIfAbsent(type, k -> new SingleComponent<>(registeredKey, component, this));
                if (singleComponent.component() != component) {
                    singleComponent.component(component);
                }
            } else if (kind == Kind.MULTI) {
                MultiComponent<T> multiComponent = (MultiComponent<T>) multiComponents
                        .computeIfAbsent(type, k -> new MultiComponent<>());
                TagComponent<T> tagComponent = new TagComponent<>(registeredKey, component, this).addTag(tags);
                multiComponent.register(registeredKey, tagComponent);
            }
        }
        return this;
    }

    private <T> T lookup(String key, Class<T> type, ScopedComponentDefinition componentDef) {
        if (matched(componentDef.scope())) {
            Kind kind = componentDef.kind();
            if (kind == Kind.SINGLE) {
                SingleComponent<T> singleComponent = getSingleComponent(type);
                if (singleComponent != null)
                    return singleComponent.component();
            } else if (kind == Kind.MULTI) {
                MultiComponent<T> multiComponent = getMultiComponent(type);
                if (multiComponent != null) {
                    SingleComponent<T> singleComponent = multiComponent.lookup(key);
                    if (singleComponent != null) {
                        return singleComponent.component();
                    }
                }
            }
        }
        return null;
    }

    private ScopedComponentRepository remove(Class<?> type, String key, ScopedComponentDefinition componentDef) {
        if (matched(componentDef.scope())) {
            Kind kind = componentDef.kind();
            if (kind == Kind.SINGLE) {
                singleComponents.remove(type);
            } else if (kind == Kind.MULTI) {
                ComponentRepository<String, ?> repository = multiComponents.get(type);
                if (repository != null) repository.remove(key);
            }
        }
        return this;
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

    private boolean matched(Scope scope) {
        return scope == Scope.UNIVERSAL || scope == this.scope;
    }

    private static Map<Class<?>, ScopedComponentDefinition> loadComponentDefinitions() {
        HashMap<Class<?>, ScopedComponentDefinition> map = new HashMap<>();
        try {
            ClassLoader classLoader = ClassUtil.getClassLoader(AbstractScopedComponentRepository.class);
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
                ScopedComponent.Scope componentScope = ScopedComponent.Scope.valueOf(split[0]);
                ScopedComponent.Kind componentKind = ScopedComponent.Kind.valueOf(split[1]);
                map.put(componentType, ScopedComponentDefinition.valueOf(componentScope, componentKind));
            }
        } catch (Exception e) {
            throw PredefinedErrorCode.COMMON.fail(e, "Failed to load " + Constant.COMPONENT_PROPERTIES_PATH);
        }
        return map;
    }

}
