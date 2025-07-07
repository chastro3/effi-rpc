package io.effi.rpc.component;


import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.resoruce.Cleanable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Manages extension loaders and extension instances within a scoped context.
 */
public class ExtensionStore implements ScopedContextOwned, ExtensionAccessor, Cleanable {

    private final Map<String, ExtensionLoader<?>> loaders = new ConcurrentHashMap<>();

    private final ScopedContext owner;

    public ExtensionStore(ScopedContext owner) {
        this.owner = AssertUtil.notNull(owner, "owner");
    }

    @Override
    public ScopedContext owner() {
        return owner;
    }    @SuppressWarnings("unchecked")
    @Override
    public <T> ExtensionLoader<T> getLoader(Class<T> type) {
        AssertUtil.notNull(type, "extension type");
        return (ExtensionLoader<T>) loaders.computeIfAbsent(type.getName(), key -> {
            ScopedComponentDescriptor descriptor = ensureScopedComponentDescriptor(type);
            return new ExtensionLoader<>(owner, type, descriptor);
        });
    }

    @Override
    public void clear() {
        loaders.values().forEach(ExtensionLoader::clear);
        loaders.clear();
    }    @Override
    public <T> T getExtension(Class<T> type, String name) {
        return getLoader(type).getExtension(name);
    }

    @Override
    public <T> T getAdaptiveExtension(Class<T> type, Function<String, String> nameGetter) {
        return getLoader(type).getAdaptiveExtension(nameGetter);
    }

    @Override
    public <T> T getDefaultExtension(Class<T> type) {
        return getLoader(type).getDefault();
    }

    @Override
    public <T> Collection<T> extensionsOf(Class<T> type, BiPredicate<String, ExtensionHolder<T>> filter) {
        return getLoader(type).extensionsOf(filter);
    }

    @Override
    public <T> Map<String, T> extensionMapOf(Class<T> type, BiPredicate<String, ExtensionHolder<T>> filter) {
        return getLoader(type).extensionMapOf(filter);
    }




}
