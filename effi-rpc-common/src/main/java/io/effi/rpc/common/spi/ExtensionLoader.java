package io.effi.rpc.common.spi;

import io.effi.rpc.common.config.Config;
import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.ClassUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.common.util.resoruce.Cleanable;
import io.effi.rpc.common.util.resoruce.Closeable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Loads and manages extensions of a given type.
 * The loader handles extension instantiation, scope management, listener notification, and cleanup.
 *
 * @param <S> the service type for which the extensions are loaded
 */
public final class ExtensionLoader<S> implements Cleanable {

    // SPI prefix path for extension resources
    private static final String PREFIX = Constant.SPI_FIX_PATH;

    private static final Map<String, Object> FAST_EXTENSION_CACHE = new ConcurrentHashMap<>();

    // Cache to store loaded ExtensionLoader instances keyed by their type name
    private static final Map<String, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    // Map storing loaded listeners for each extension type
    private static final Map<Class<?>, Set<LoadedListener<?>>> LISTENERS = new ConcurrentHashMap<>();

    // ExtensionFactory for get extension instances
    private static final DelegateExtensionFactory EXTENSION_FACTORY = new DelegateExtensionFactory();

    // Map of extension names to their respective ExtensionWrapper instances
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

    /**
     * Private constructor for initializing an ExtensionLoader.
     * It loads extension wrappers and prepares for extension instantiation.
     *
     * @param type the extension interface class
     */
    private ExtensionLoader(Class<S> type) {
        this.type = type;
        Extensible extensible = type.getAnnotation(Extensible.class);
        this.defaultExtension = extensible.value();
        this.lazyLoad = extensible.lazyLoad();
        this.key = extensible.key();
        loadExtensionWrappers(type);
    }

    /**
     * Adds the extension factory for creating extension instances.
     *
     * @param extensionFactory
     */
    public static void addExtensionFactory(ExtensionFactory extensionFactory) {
        EXTENSION_FACTORY.add(extensionFactory);
    }

    /**
     * Load the ExtensionLoader for the specified type.
     * It checks for validity of the type and ensures the class is annotation with @Extensible.
     *
     * @param type the extension interface class
     * @param <S>  the type of the extension
     * @return the loaded ExtensionLoader instance
     */
    @SuppressWarnings("unchecked")
    public static <S> ExtensionLoader<S> load(Class<S> type) {
        AssertUtil.notNull(type, "extension type");
        AssertUtil.condition(type.isInterface(), "extension type (" + type + ") cannot be an interface");
        AssertUtil.condition(type.isAnnotationPresent(Extensible.class), "extension type (" + type + "): missing @Extensible annotation");
        return (ExtensionLoader<S>) LOADERS.computeIfAbsent(type.getTypeName(), k -> new ExtensionLoader<>(type));
    }

    /**
     * Loads an extension instance using a provided URL, based on a key defined in @Extensible.
     *
     * @param type   the extension interface class
     * @param config the config containing extension configuration
     * @param <S>    the type of the extension
     * @return the loaded extension instance
     */
    public static <S> S loadExtension(Class<S> type, Config config) {
        ExtensionLoader<S> extensionLoader = load(type);
        String key = extensionLoader.key;
        String name = StringUtil.isBlank(key)
                ? extensionLoader.defaultExtension
                : config.getOrDefault(key, extensionLoader.defaultExtension);
        return extensionLoader.getExtension(name);

    }

    /**
     * Loads an extension instance by its name.
     *
     * @param type          the extension interface class
     * @param extensionName the name of the extension to load
     * @param <S>           the type of the extension
     * @return the loaded extension instance
     */
    @SuppressWarnings("unchecked")
    public static <S> S loadExtension(Class<S> type, String extensionName) {
        return (S) FAST_EXTENSION_CACHE.computeIfAbsent(fastExtensionCacheKey(type, extensionName),
                k -> load(type).getExtension(extensionName));
    }

    /**
     * Loads the default extension for the given type.
     *
     * @param type the extension interface class
     * @param <S>  the type of the extension
     * @return the default extension instance
     */
    public static <S> S loadExtension(Class<S> type) {
        return load(type).getDefault();
    }

    /**
     * Loads all available extensions for the given type.
     *
     * @param type the extension interface class
     * @param <S>  the type of the extension
     * @return a list of loaded extension instances
     */
    public static <S> List<S> loadExtensions(Class<S> type) {
        return load(type).getExtensions();
    }

    /**
     * Adds listeners to a specific extension interface.
     * Listeners are invoked when an extension instance is created.
     *
     * @param interfaceType   the extension interface class
     * @param loadedListeners the listeners to be registered
     * @param <S>             the type of the extension
     */
    @SafeVarargs
    public static <S> void addListener(Class<S> interfaceType, LoadedListener<S>... loadedListeners) {
        if (CollectionUtil.isNotEmpty(loadedListeners)) {
            Set<LoadedListener<?>> loadedListenerSet = LISTENERS.computeIfAbsent(interfaceType, k -> new HashSet<>());
            Collections.addAll(loadedListenerSet, loadedListeners);
        }
    }

    /**
     * Clears all loaded extension instances and listeners.
     */
    public static void clearLoader() {
        LISTENERS.clear();
        LOADERS.values().forEach(ExtensionLoader::clear);
        LOADERS.clear();
    }

    private static String fastExtensionCacheKey(Class<?> type, String name) {
        return type.getName() + '@' + name;
    }

    /**
     * Loads all available extensions for the specified type by scanning resources.
     *
     * @param type the extension interface class
     */
    @SuppressWarnings("unchecked")
    private void loadExtensionWrappers(Class<S> type) {
        try {
            ClassLoader classLoader = ClassUtil.getClassLoader(type);
            Enumeration<URL> resources = classLoader.getResources(PREFIX + type.getTypeName());
            List<ExtensionWrapper> extensionsWrappers = new ArrayList<>();
            List<ExtensionWrapper> overrideExtensionsWrappers = new ArrayList<>();
            // Load and create ExtensionWrapper instances for all valid extensions
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String extensionClassName;
                    while ((extensionClassName = br.readLine()) != null) {
                        Class<?> extensionClass = classLoader.loadClass(extensionClassName);
                        if (this.type.isAssignableFrom(extensionClass) && extensionClass.isAnnotationPresent(Extension.class)) {
                            ExtensionWrapper extensionWrapper = new ExtensionWrapper((Class<? extends S>) extensionClass);
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
     *
     * @param extensionName the name of the extension
     * @return the extension instance
     */
    public S getExtension(String extensionName) {
        ExtensionWrapper wrapper = extensionWrappers.get(extensionName);
        if (wrapper == null) {
            throw new IllegalArgumentException(String.format(
                    "Failed to load extension '%s' for %s: not found or does not meet loading conditions.",
                    extensionName, this.type.getTypeName()
            ));
        }
        return wrapper.instance();
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

    /**
     * Clears all extension instances and wrappers.
     */
    @Override
    public void clear() {
        extensionWrappers.values().forEach(ExtensionWrapper::clear);
        extensionWrappers.clear();
    }

    /**
     * A wrapper class for managing the instantiation and lifecycle of extensions.
     */
    class ExtensionWrapper implements Cleanable {

        // Extension implementation class type
        private final Class<? extends S> type;

        // Extension annotation metadata
        private final Extension extension;

        // List of extension values
        private final List<String> values;

        // Singleton instance (if applicable)
        private volatile S instance;

        private boolean cleared = false;

        /**
         * Constructs an ExtensionWrapper for the given extension class.
         *
         * @param type the extension implementation class
         */
        ExtensionWrapper(Class<? extends S> type) {
            this.type = type;
            this.extension = type.getAnnotation(Extension.class);
            this.values = Arrays.stream(extension.value()).distinct().toList();
            if (!lazyLoad && extension.scope() == Scope.SINGLETON) {
                instance = newInstance();
            }
        }

        /**
         * Checks whether the extension condition is met.
         */
        public boolean isConditionMet() {
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

        /**
         * Retrieves the extension instance, creating it if necessary.
         */
        public S instance() {
            if (extension.scope() == Scope.SINGLETON) {
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

        @SuppressWarnings("unchecked")
        private S newInstance() {
            S instance = EXTENSION_FACTORY.getExtension(type, values);
            // Trigger all listeners for this type
            LISTENERS.getOrDefault(type, Collections.emptySet())
                    .forEach(listener -> ((LoadedListener<S>) listener).onLoaded(instance));
            return instance;
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
}
