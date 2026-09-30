package io.effi.rpc.component.extension;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedContext;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.TagComponent;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.trait.Ordered;
import io.effi.rpc.util.ReflectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.trait.Cleanable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * Manages a single extension implementation.
 * <p>
 * Encapsulates metadata and lifecycle logic, including instantiation,scope management,
 * condition evaluation, and cleanup.
 * <p>
 * Supports singleton and prototype scopes, and handles scoped context injection when applicable.
 */
public final class ExtensionEntry<T> implements TagComponent, Cleanable, Ordered {

    private final ExtensionLoader<T> loader;

    private final Class<? extends T> type;

    private final Extension extension;

    private final String[] names;

    private final Set<String> tags;

    private boolean cleared = false;

    private volatile T instance;

    ExtensionEntry(ExtensionLoader<T> loader, Class<? extends T> type) {
        this.loader = loader;
        this.type = type;
        this.extension = AssertUtil.requireAnnotation(type, Extension.class);
        this.names = StringUtil.deduplicate(extension.value());
        this.tags = Set.of(extension.tags());
    }

    @Override
    public Set<String> tags() {
        return tags;
    }

    @Override
    public int order() {
        return extension.order();
    }

    public String[] names() {
        return names;
    }

    public boolean containName(String name) {
        for (String item : names) {
            if (Objects.equals(item, name)) return true;
        }
        return false;
    }

    public boolean singleton() {
        return extension.scope() == Extension.Scope.SINGLETON;
    }

    public T extension() {
        if (singleton()) {
            T result = instance;
            if (result != null) return result;
            synchronized (this) {
                ensureActive();
                result = instance;
                return result != null ? result : (instance = newExtension());
            }
        }
        synchronized (this) {
            ensureActive();
            return newExtension();
        }
    }

    @Override
    public synchronized void clear() {
        if (cleared) {
            return;
        }
        cleared = true;
        T current = instance;
        instance = null;
        if (current != null) {
            ObjectUtil.release(current);
        }
    }


    @Override
    public String toString() {
        return ObjectUtil.simpleClassName(this)
                + "<" + type.getSimpleName() + "> {names="
                + Arrays.toString(names) + "}";
    }

    boolean available() {
        String[] classes = extension.onClass();
        if (CollectionUtil.isEmpty(classes)) return true;
        try {
            for (String type : classes) {
                ClassUtil.findClassLoader(this.type).loadClass(type);
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    boolean canOverride() {
        return extension.override();
    }

    boolean primary() {
        return extension.primary();
    }

    @SuppressWarnings("unchecked")
    private T newExtension() {
        ScopedContext scopedContext = loader.scopedContext();
        Class<T> extensibleType = loader.type();
        T extension = null;
        // 1. Try to find the extension from current scoped context
        if (names.length == 1) {
            extension = scopedContext.namedComponent(extensibleType, names[0]);
        } else {
            for (String name : names) {
                extension = scopedContext.namedComponent(extensibleType, name);
                if (extension != null) break;
            }
        }
        // 2. If not found, it is created by reflection
        if (extension == null) {
            extension = ReflectionUtil.newInstance(type);
            if (singleton()) {
                for (String name : names) {
                    scopedContext.registry().register(extensibleType, name, extension);
                }
            }
        }
        T finalInstance = extension;
        injectScopedContext(extension, scopedContext);
        // Trigger all listeners for this type
        scopedContext.components(ExtensionLoadedListener.class, this::matchesExtensionType)
                .forEach(listener -> ((ExtensionLoadedListener<T>) listener).onLoaded(finalInstance));
        return finalInstance;
    }

    private boolean matchesExtensionType(String name, ExtensionLoadedListener<?> listener) {
        Class<?> extensionType = listener.extensionType();
        return extensionType != null && extensionType.isAssignableFrom(type);
    }

    private void injectScopedContext(Object target, ScopedContext context) {
        if (target instanceof ScopedPlatform.Acceptor acceptor
                && context instanceof ScopedPlatform platform) {
            acceptor.accept(platform);
        } else if (target instanceof ScopedApplication.Acceptor acceptor
                && context instanceof ScopedApplication application) {
            acceptor.accept(application);
        } else if (target instanceof ScopedModule.Acceptor acceptor
                && context instanceof ScopedModule module) {
            acceptor.accept(module);
        }
    }

    private void ensureActive() {
        if (cleared) {
            throw new IllegalStateException("Extension '" + type.getName() + "' is already cleared");
        }
    }
}
