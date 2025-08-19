package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.constant.ResourcePath;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.Messages;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope;
import static io.effi.rpc.util.ObjectUtil.annotationName;
import static io.effi.rpc.util.ObjectUtil.simpleClassName;

/**
 * Describes component scopes, kinds, and their associated scoped context types.
 * <p>
 * Manages component metadata including scope and kind information,
 * with support for lookup and validation of component descriptors.
 *
 * @see ScopedComponent
 */
public final class ComponentDescriptor {

    public static final String DESCRIPTOR_FILE = ResourcePath.COMPONENT_DESCRIPTOR_FILE;

    private static final EnumMap<Scope, EnumMap<Kind, ComponentDescriptor>> CACHE = createCache();

    private static final Map<Class<?>, ComponentDescriptor> COMPONENT_DESCRIPTORS = loadComponentDescriptors();

    private final Scope scope;

    private final Kind kind;

    private ComponentDescriptor(Scope scope, Kind kind) {
        this.scope = scope;
        this.kind = kind;
    }

    /**
     * Finds supported component types that are assignable from the given type.
     *
     * @param type the base type to check for supported components
     * @return the list of supported component types, or empty list if none found
     */
    public static List<Class<?>> findSupportedComponentTypes(Class<?> type) {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> defType : COMPONENT_DESCRIPTORS.keySet()) {
            if (defType.isAssignableFrom(type)) {
                result.add(defType);
            }
        }
        return result.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(result);
    }

    /**
     * Ensures a component descriptor exists for the given type and scoped context.
     *
     * @param type           the component type to validate
     * @param scopedContext  the scoped context to check compatibility
     * @return the component descriptor if valid
     * @throws IllegalArgumentException if descriptor is missing or scope is incompatible
     */
    public static ComponentDescriptor ensure(Class<?> type, ScopedContext scopedContext) {
        ComponentDescriptor descriptor = lookup(type);
        if (descriptor == null) {
            throw new IllegalArgumentException("Component descriptor for '" + type.getName()
                    + "' is missing.Please ensure the type is annotated with " + annotationName(ScopedComponent.class)
                    + "and that the compile-time metadata was generated in " + DESCRIPTOR_FILE + ".");
        }
        if (!scopedContext.matchesScope(descriptor.scope())) {
            throw new IllegalArgumentException("Component '" + type.getName() + "' is not supported in '"
                    + simpleClassName(scopedContext) + "'. Please use '" + simpleClassName(descriptor.scopedContextType())
                    + "' to operate.");
        }
        return descriptor;
    }

    public static ComponentDescriptor lookup(Class<?> type) {
        return COMPONENT_DESCRIPTORS.get(type);
    }

    public Scope scope() {
        return scope;
    }

    public Kind kind() {
        return kind;
    }

    public Class<? extends ScopedContext> scopedContextType() {
        return switch (scope) {
            case PLATFORM -> ScopedPlatform.class;
            case APPLICATION -> ScopedApplication.class;
            case MODULE -> ScopedModule.class;
            default -> ScopedContext.class;
        };
    }

    public boolean isSingle() {
        return kind == Kind.SINGLE;
    }

    private static EnumMap<Scope, EnumMap<Kind, ComponentDescriptor>> createCache() {
        EnumMap<Scope, EnumMap<Kind, ComponentDescriptor>> cache = new EnumMap<>(Scope.class);
        for (Scope s : Scope.values()) {
            EnumMap<Kind, ComponentDescriptor> inner = new EnumMap<>(Kind.class);
            for (Kind k : Kind.values()) {
                inner.put(k, new ComponentDescriptor(s, k));
            }
            cache.put(s, inner);
        }
        return cache;
    }

    private static ComponentDescriptor cacheOf(Scope scope, Kind kind) {
        return CACHE.get(scope).get(kind);
    }

    private static Map<Class<?>, ComponentDescriptor> loadComponentDescriptors() {
        HashMap<Class<?>, ComponentDescriptor> map = new HashMap<>();
        try {
            ClassLoader classLoader = ClassUtil.findClassLoader(ScopedContext.class);
            Properties properties = new Properties();
            Enumeration<URL> resources = classLoader.getResources(DESCRIPTOR_FILE);
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
                map.put(componentType, cacheOf(componentScope, componentKind));
            }
        } catch (Exception e) {
            throw new IllegalStateException(Messages.parseFile(DESCRIPTOR_FILE), e);
        }
        return map;
    }
}