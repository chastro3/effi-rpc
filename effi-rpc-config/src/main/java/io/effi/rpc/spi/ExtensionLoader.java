package io.effi.rpc.spi;

import io.effi.rpc.annotation.spi.Extensible;
import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Pair;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.resoruce.Cleanable;
import io.effi.rpc.util.resoruce.Closeable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loads and manages extensions for a given type.
 * Handles instantiation, scope management, listener notification, and cleanup.
 */
public final class ExtensionLoader<S> implements Cleanable {

    // SPI prefix path for extension resources
    private static final String PREFIX = Constant.SPI_FIX_PATH;

    private static final Map<Pair<Class<?>, String>, Object> FAST_EXTENSION_CACHE = new ConcurrentHashMap<>();

    // Cache to store loaded ExtensionLoader instance keyed by their type name
    private static final Map<String, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    // Map storing loaded listeners for each extension type
    private static final Map<Class<?>, Set<LoadedListener<?>>> LISTENERS = new ConcurrentHashMap<>();

    // ExtensionFactory for get extension instance
    private static final DelegateExtensionFactory EXTENSION_FACTORY = new DelegateExtensionFactory();

    // Map of extension names to their respective ExtensionWrapper instance
    private final Map<String, ExtensionWrapper> extensionWrappers = new LinkedHashMap<>();

    // Type of the extension (interface)
    private final Class<S> type;

    // Default extension name
    private final String defaultExtension;

    // Flag indicating if the extension should be loaded lazily
    private final boolean lazyLoad;

    // Key for retrieving extension from URL parameters
    private final String key;

    static {
        ServiceLoader.load(ExtensionLoaderClassInitializer.class)
                .forEach(ExtensionLoaderClassInitializer::initialize);
    }

    private ExtensionLoader(Class<S> type, Extensible extensible) {
        this.type = type;
        this.defaultExtension = extensible.value();
        this.lazyLoad = extensible.lazyLoad();
        this.key = extensible.key();
        loadExtensionWrappers(type);
    }

    /**
     * Adds the extension factory for creating extension instance.
     */
    public static void addExtensionFactory(ExtensionFactory extensionFactory) {
        EXTENSION_FACTORY.add(extensionFactory);
    }

    /**
     * Load the ExtensionLoader for the specified type.
     */
    @SuppressWarnings("unchecked")
    public static <S> ExtensionLoader<S> load(Class<S> type) {
        AssertUtil.notNull(type, "extension type");
        AssertUtil.condition(type.isInterface(), "extension type (" + type + ") cannot be an interface");
        Extensible extensible = AssertUtil.notNull(type.getAnnotation(Extensible.class), "extension type (" + type + "): missing @Extensible annotation");
        return (ExtensionLoader<S>) LOADERS.computeIfAbsent(type.getTypeName(), k -> new ExtensionLoader<>(type, extensible));
    }

    /**
     * Loads an extension instance by its name.
     */
    @SuppressWarnings("unchecked")
    public static <S> S loadExtension(Class<S> type, String extensionName) {
        Pair<Class<?>, String> key = Pair.of(type, extensionName);
        return (S) FAST_EXTENSION_CACHE.computeIfAbsent(key, k -> load(type).getExtension(extensionName));
    }

    /**
     * Loads an adaptive extension instance.
     */
    public static <S> S loadAdaptiveExtension(Class<S> type, Function<String, String> nameGetter) {
        return load(type).getAdaptiveExtension(nameGetter);
    }

    /**
     * Loads the default extension for the given type.
     */
    public static <S> S loadExtension(Class<S> type) {
        return load(type).getDefault();
    }

    /**
     * Loads all available extensions for the given type.
     */
    public static <S> List<S> loadExtensions(Class<S> type) {
        return load(type).getExtensions();
    }

    /**
     * Adds listeners to a specific extension interface.
     * Listeners are invoked when an extension instance is created.
     */
    @SafeVarargs
    public static <S> void addListener(Class<S> interfaceType, LoadedListener<S>... loadedListeners) {
        if (CollectionUtil.isNotEmpty(loadedListeners)) {
            Set<LoadedListener<?>> loadedListenerSet = LISTENERS.computeIfAbsent(interfaceType, k -> new HashSet<>());
            Collections.addAll(loadedListenerSet, loadedListeners);
        }
    }

    /**
     * Clears all loaded extension instance and listeners.
     */
    public static void clearLoader() {
        LISTENERS.clear();
        LOADERS.values().forEach(ExtensionLoader::clear);
        LOADERS.clear();
    }

    /**
     * Loads all available extensions for the specified type by scanning resources.
     */
    @SuppressWarnings("unchecked")
    private void loadExtensionWrappers(Class<S> type) {
        try {
            ClassLoader classLoader = ClassUtil.getClassLoader(type);
            Enumeration<URL> resources = classLoader.getResources(PREFIX + type.getTypeName());
            List<ExtensionWrapper> extensionsWrappers = new ArrayList<>();
            List<ExtensionWrapper> overrideExtensionsWrappers = new ArrayList<>();
            // Load and create ExtensionWrapper instance for all valid extensions
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String extensionClassName;
                    while ((extensionClassName = br.readLine()) != null) {
                        Class<?> extensionClass = classLoader.loadClass(extensionClassName);
                        Extension extension = extensionClass.getAnnotation(Extension.class);
                        if (this.type.isAssignableFrom(extensionClass) && extension != null) {
                            ExtensionWrapper extensionWrapper = new ExtensionWrapper((Class<? extends S>) extensionClass, extension);
                            if (extensionWrapper.isConditionMet()) {
                                extensionsWrappers.add(extensionWrapper);
                                if (extensionWrapper.extension.override()) {
                                    overrideExtensionsWrappers.add(extensionWrapper);
                                }
                            }
                        }
                    }
                }
            }
            extensionsWrappers = extensionsWrappers.stream()
                    .sorted(Comparator.comparingInt(wrapper -> wrapper.extension.order()))
                    .collect(Collectors.toList());
            // Register the extension wrappers
            for (ExtensionWrapper extensionsWrapper : extensionsWrappers) {
                for (String value : extensionsWrapper.values) {
                    this.extensionWrappers.put(value, extensionsWrapper);
                }
            }
            // Register override extension wrappers
            for (ExtensionWrapper overrideExtensionsWrapper : overrideExtensionsWrappers) {
                for (String value : overrideExtensionsWrapper.values) {
                    this.extensionWrappers.put(value, overrideExtensionsWrapper);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Retrieves the extension by name.
     */
    public S getExtension(String extensionName) {
        ExtensionWrapper wrapper = extensionWrappers.get(extensionName);
        if (wrapper == null) {
            throw new IllegalArgumentException(StringUtil.format(
                    "Failed to load extension '{}' for {}: not found or not eligible for loading.",
                    extensionName, type.getTypeName()
            ));
        }
        return wrapper.instance();
    }

    public S getAdaptiveExtension(Function<String, String> nameGetter) {
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
     * Retrieves the default extension.
     */
    public S getDefault() {
        return getExtension(defaultExtension);
    }

    /**
     * Retrieves all loaded extensions.
     */
    public List<S> getExtensions() {
        ArrayList<S> list = new ArrayList<>();
        for (String key : extensionWrappers.keySet()) {
            list.add(getExtension(key));
        }
        return list.isEmpty() ? Collections.emptyList() : list;
    }

    @Override
    public void clear() {
        extensionWrappers.values().forEach(ExtensionWrapper::clear);
        extensionWrappers.clear();
    }

    /**
     * Manages the instantiation and lifecycle of extensions.
     */
    class ExtensionWrapper implements Cleanable {

        private final Class<? extends S> type;

        private final Extension extension;

        private final String[] values;

        private volatile S instance;

        private boolean cleared = false;

        ExtensionWrapper(Class<? extends S> type, Extension extension) {
            this.type = type;
            this.extension = extension;
            this.values = CollectionUtil.deduplicate(extension.value());
            if (!lazyLoad && extension.scope() == Extension.Scope.SINGLETON) {
                instance = newInstance();
            }
        }

        boolean isConditionMet() {
            String[] classes = extension.onClass();
            if (CollectionUtil.isEmpty(classes)) {
                return true;
            }
            try {
                for (String type : classes) {
                    ClassUtil.getClassLoader(this.type).loadClass(type);
                }
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        S instance() {
            if (extension.scope() == Extension.Scope.SINGLETON) {
                if (instance == null) {
                    synchronized (this) {
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

        @SuppressWarnings("unchecked")
        private S newInstance() {
            S instance = EXTENSION_FACTORY.getExtension(type, values);
            // Trigger all listeners for this type
            LISTENERS.getOrDefault(type, Collections.emptySet())
                    .forEach(listener -> ((LoadedListener<S>) listener).onLoaded(instance));
            return instance;
        }

    }
}
