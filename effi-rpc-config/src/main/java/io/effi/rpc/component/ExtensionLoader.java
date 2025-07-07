package io.effi.rpc.component;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.resoruce.Cleanable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loads and manages extensions for a given type.
 * Handles instantiation, scope management, listener notification, and cleanup.
 */
public final class ExtensionLoader<T> implements Cleanable {

    private static final String PREFIX = Constant.SPI_FIX_PATH;

    private static final Map<Class<?>, ExtensionHolder<?>> EXTENSION_HOLDERS = new ConcurrentHashMap<>();

    private final LinkedHashMap<String, ExtensionHolder<T>> extensionHolders;

    private final ScopedContext scopedContext;

    private final ScopedComponentDescriptor descriptor;

    private final Class<T> type;

    private final String defaultExtension;

    private final boolean lazyLoad;

    private final String key;

    ExtensionLoader(ScopedContext scopedContext, Class<T> type, ScopedComponentDescriptor descriptor) {
        Extensible extensible = checkExtensible(type);
        this.scopedContext = scopedContext;
        this.type = type;
        this.descriptor = descriptor;
        this.defaultExtension = extensible.value();
        this.lazyLoad = extensible.lazyLoad();
        this.key = extensible.key();
        this.extensionHolders = loadExtensionHolders(type);
    }

    private Extensible checkExtensible(Class<?> type) {
        AssertUtil.condition(type.isInterface(), "extension type '{}' must be an interface", type.getName());
        return AssertUtil.notAnnotation(type, Extensible.class);
    }

    /**
     * Loads all available extensions for the specified type by scanning resources.
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, ExtensionHolder<T>> loadExtensionHolders(Class<T> type) {
        String path = PREFIX + type.getTypeName();
        try {
            ClassLoader classLoader = ClassUtil.getClassLoader(type);
            Enumeration<URL> resources = classLoader.getResources(path);
            List<ExtensionHolder<T>> extensionHolders = new ArrayList<>();
            List<ExtensionHolder<T>> overrideExtensionHolders = new ArrayList<>();
            // Load and create ExtensionWrapper instance for all valid extensions
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String extensionClassName;
                    while ((extensionClassName = br.readLine()) != null) {
                        Class<?> extensionClass = classLoader.loadClass(extensionClassName);
                        Extension extension = extensionClass.getAnnotation(Extension.class);
                        if (type.isAssignableFrom(extensionClass) && extension != null) {
                            ExtensionHolder<T> extensionHolder = (ExtensionHolder<T>) EXTENSION_HOLDERS.computeIfAbsent(extensionClass, k ->
                                    new ExtensionHolder<>(this, (Class<? extends T>) extensionClass, extension)
                            );
                            if (extensionHolder.isConditionMet()) {
                                extensionHolders.add(extensionHolder);
                                if (extensionHolder.canOverride()) {
                                    overrideExtensionHolders.add(extensionHolder);
                                }
                            }
                        }
                    }
                }
            }
            LinkedHashMap<String, ExtensionHolder<T>> result = new LinkedHashMap<>();
            extensionHolders = extensionHolders.stream()
                    .sorted(Comparator.comparingInt(ExtensionHolder::order))
                    .collect(Collectors.toList());
            // Register the extension holders
            for (ExtensionHolder<T> extensionHolder : extensionHolders) {
                for (String value : extensionHolder.names()) {
                    result.put(value, extensionHolder);
                }
            }
            // Register override extension holders
            for (ExtensionHolder<T> overrideExtensionHolder : overrideExtensionHolders) {
                for (String value : overrideExtensionHolder.names()) {
                    result.put(value, overrideExtensionHolder);
                }
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(Messages.parseFile(path), e);
        }
    }

    public Class<T> type() {
        return type;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public ScopedContext scopedContext() {
        return scopedContext;
    }

    public ScopedComponentDescriptor scopedComponentDescriptor() {
        return descriptor;
    }

    public T getAdaptiveExtension(Function<String, String> nameGetter) {
        AssertUtil.notNull(nameGetter, "nameGetter");
        // todo 扩展key的优化
        if (StringUtil.isBlank(key)) {
            throw new IllegalStateException("Failed to load adaptive extension for "
                    + type.getTypeName() + ": no key defined in @Extensible");
        }
        String name = nameGetter.apply(key);
        if (StringUtil.isBlank(name)) name = defaultExtension;
        return getExtension(name);
    }

    /**
     * Retrieves the extension by name.
     */
    public T getExtension(String extensionName) {
        ExtensionHolder<T> holder = extensionHolders.get(extensionName);
        if (holder == null) {
            throw new IllegalArgumentException(StringUtil.format(
                    "Failed to load extension '{}' for '{}': not found or not eligible for loading.",
                    extensionName, type.getTypeName()
            ));
        }
        return holder.instance();
    }

    /**
     * Retrieves the default extension.
     */
    public T getDefault() {
        return getExtension(defaultExtension);
    }

    /**
     * Retrieves all loaded extensions.
     */
    public Collection<T> extensionsOf(BiPredicate<String, ExtensionHolder<T>> filter) {
        return extensionMapOf(filter).values();
    }

    public Map<String, T> extensionMapOf(BiPredicate<String, ExtensionHolder<T>> filter) {
        return CollectionUtil.unmodifiable(extensionHolders, filter, ExtensionHolder::instance);
    }

    @Override
    public void clear() {
        extensionHolders.values().forEach(ExtensionHolder::clear);
        extensionHolders.clear();
    }

}
