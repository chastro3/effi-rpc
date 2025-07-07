package io.effi.rpc.component;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Ordered;
import io.effi.rpc.util.ReflectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.resoruce.Cleanable;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.Objects;
import java.util.Set;

/**
 * Manages the instantiation and lifecycle of extensions.
 */
public class ExtensionHolder<T> implements TagComponent, Cleanable, Ordered {

    private final Object lock = new Object();

    private final ExtensionLoader<T> loader;

    private final Class<? extends T> type;

    private final Extension extension;

    private final String[] names;

    private final Set<String> tags;

    private boolean cleared = false;

    private volatile T instance;

    ExtensionHolder(ExtensionLoader<T> loader, Class<? extends T> type, Extension extension) {
        this.loader = loader;
        this.type = type;
        this.extension = extension;
        this.names = StringUtil.deduplicate(extension.value());
        this.tags = Set.of(extension.tags());
        if (!loader.isLazyLoad() && extension.scope() == Extension.Scope.SINGLETON) {
            this.instance = newInstance();
        }
    }

    @SuppressWarnings("unchecked")
    private T newInstance() {
        ScopedContext scopedContext = loader.scopedContext();
        T instance = null;
        for (String name : names) {
            instance = scopedContext.lookup(loader.type(), name);
            if (instance != null) {
                break;
            }
        }
        if (instance == null) {
            instance = ReflectionUtil.newInstance(type);
            if (extension.scope() == Extension.Scope.SINGLETON) {
                for (String name : names) {
                    scopedContext.register(loader.type(), name, instance);
                }
            }
        }
        T finalInstance = instance;
        // Trigger all listeners for this type
        scopedContext.listOf(ExtensionLoadedListener.class, this::matchesExtensionType)
                .forEach(listener -> ((ExtensionLoadedListener<T>) listener).onLoaded(finalInstance));
        return finalInstance;
    }

    private boolean matchesExtensionType(String name, ExtensionLoadedListener<?> listener) {
        Class<?> extensionType = listener.extensionType();
        return extensionType != null && extensionType.isAssignableFrom(type);
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

    public boolean isConditionMet() {
        String[] classes = extension.onClass();
        if (CollectionUtil.isEmpty(classes)) return true;
        try {
            for (String type : classes) {
                ClassUtil.getClassLoader(this.type).loadClass(type);
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public boolean canOverride() {
        return extension.override();
    }

    @Override
    public Set<String> tags() {
        return tags;
    }

    @Override
    public int order() {
        return extension.order();
    }

    public T instance() {
        if (extension.scope() == Extension.Scope.SINGLETON) {
            if (instance == null) {
                synchronized (lock) {
                    if (instance == null) {
                        instance = newInstance();
                    }
                }
            }
            return instance;
        } else {
            return newInstance();
        }
    }

    @Override
    public void clear() {
        if (instance != null && !cleared) {
            cleared = true;
            if (instance instanceof Closeable closeable) {
                closeable.close();
            } else if (instance instanceof Cleanable cleanable) {
                cleanable.clear();
            }
        }
    }

    @Override
    public String toString() {
        return type.toString();
    }
}