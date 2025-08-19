package io.effi.rpc.component.extension;

import io.effi.rpc.component.ComponentDescriptor;
import io.effi.rpc.component.ScopedContext;
import io.effi.rpc.component.ScopedContextOwned;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.resoruce.Cleanable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Manages extension loaders and their extensions within a scoped context.
 * <p>
 * Registers {@link ExtensionLoader} instances and maps extension types to their loaders.
 * Integrates with {@link Cleanable} for lifecycle handling.
 * <p>
 * Binds to a specific {@link ScopedContext} and provides scoped access to extensions.
 */
public class ExtensionRepository implements ScopedContextOwned, ExtensionAccessor, Cleanable {

    private final Map<Class<?>, ExtensionLoader<?>> loaders = new ConcurrentHashMap<>();

    private final ScopedContext owner;

    public ExtensionRepository(ScopedContext owner) {
        this.owner = AssertUtil.notNull(owner, "owner");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ExtensionLoader<T> extensionLoader(Class<T> type) {
        AssertUtil.notNull(type, "extension type");
        return (ExtensionLoader<T>) loaders.computeIfAbsent(type, k -> {
            ComponentDescriptor descriptor = ensureComponentDescriptor(type);
            return new ExtensionLoader<>(owner, type, descriptor);
        });
    }

    @Override
    public <T> T preferredExtension(Class<T> type, String name) {
        return extensionLoader(type).preferredExtension(name);
    }

    @Override
    public <T> T namedExtension(Class<T> type, String name) {
        return extensionLoader(type).namedExtension(name);
    }

    @Override
    public <T> T adaptiveExtension(Class<T> type, Function<String, String> nameGetter) {
        return extensionLoader(type).adaptiveExtension(nameGetter);
    }

    @Override
    public <T> T primaryExtension(Class<T> type) {
        return extensionLoader(type).primaryExtension();
    }

    @Override
    public <T> Collection<T> extensions(Class<T> type, BiPredicate<String, ExtensionEntry<T>> filter) {
        return extensionLoader(type).extensions(filter);
    }

    @Override
    public <T> Map<String, T> namedExtensions(Class<T> type, BiPredicate<String, ExtensionEntry<T>> filter) {
        return extensionLoader(type).namedExtensions(filter);
    }

    @Override
    public ScopedContext owner() {
        return owner;
    }

    @Override
    public void clear() {
        loaders.values().forEach(ExtensionLoader::clear);
        loaders.clear();
    }
}
