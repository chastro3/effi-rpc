package io.effi.rpc.component.extension;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ComponentDescriptor;
import io.effi.rpc.component.ScopedContext;
import io.effi.rpc.constant.ResourcePaths;
import io.effi.rpc.trait.Cleanable;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

import static io.effi.rpc.util.StringUtil.format;

/**
 * Loads and manages extension components of a specified type.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ExtensionLoader<T> implements Cleanable {

    private static final Map<Class<?>, Map<String, Class>> CACHE = new ConcurrentHashMap<>();

    private final Map<String, ExtensionEntry<T>> extensionEntries;

    private final ScopedContext scopedContext;

    private final ComponentDescriptor descriptor;

    private final Class<T> type;

    private final String primaryExtension;

    private final boolean lazyLoaded;

    private volatile boolean loaded;

    private boolean loading;

    ExtensionLoader(ScopedContext scopedContext, Class<T> type, ComponentDescriptor descriptor) {
        this.scopedContext = scopedContext;
        this.type = type;
        this.descriptor = descriptor;
        Extensible extensible = requireExtensible(type);
        Map<String, Class<? extends T>> extensionClasses = loadExtensionClasses(type);
        this.lazyLoaded = extensible.lazyLoad();
        this.extensionEntries = createEntries(extensionClasses);
        this.primaryExtension = findPrimaryExtension(extensible, extensionClasses);
    }

    private static Extensible requireExtensible(Class<?> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type '" + type.getName() + "' must be an interface");
        }
        return AssertUtil.requireAnnotation(type, Extensible.class);
    }

    private static <T> Map<String, Class<? extends T>> loadExtensionClasses(Class<T> type) {
        return (Map<String, Class<? extends T>>) (Map<?, ?>) CACHE.computeIfAbsent(type, ExtensionLoader::scanExtensionClasses);
    }

    private Map<String, ExtensionEntry<T>> createEntries(Map<String, Class<? extends T>> extensionClasses) {
        Map<Class<?>, ExtensionEntry<T>> entriesByType = new HashMap<>();
        LinkedHashMap<String, ExtensionEntry<T>> result = new LinkedHashMap<>();
        extensionClasses.forEach((name, extensionClass) -> {
            ExtensionEntry<T> entry = entriesByType.computeIfAbsent(extensionClass, ignored ->
                    new ExtensionEntry<>(this, extensionClass)
            );
            result.put(name, entry);
        });
        return result;
    }

    private static <T> String findPrimaryExtension(Extensible extensible, Map<String, Class<? extends T>> entries) {
        if (StringUtil.isNotBlank(extensible.value())) {
            return extensible.value();
        }
        return entries.entrySet().stream()
                .filter(entry -> entry.getValue().getAnnotation(Extension.class).primary())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private static Map<String, Class> scanExtensionClasses(Class<?> rawType) {
        String path = ResourcePaths.SPI_SERVICES_DIR + rawType.getTypeName();
        try {
            ClassLoader classLoader = ClassUtil.findClassLoader(rawType);
            Enumeration<URL> resources = classLoader.getResources(path);
            Map<Class<?>, Extension> definitionsByType = new HashMap<>();
            Set<Class<?>> normalTypes = new LinkedHashSet<>();
            Set<Class<?>> overrideTypes = new LinkedHashSet<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String className;
                    while ((className = reader.readLine()) != null) {
                        Class<?> extensionClass = classLoader.loadClass(className);
                        Extension extension = extensionClass.getAnnotation(Extension.class);
                        if (!rawType.isAssignableFrom(extensionClass) || extension == null) {
                            continue;
                        }
                        definitionsByType.putIfAbsent(extensionClass, extension);
                        if (!available(extension, classLoader)) {
                            continue;
                        }
                        if (extension.override()) {
                            overrideTypes.add(extensionClass);
                        } else {
                            normalTypes.add(extensionClass);
                        }
                    }
                }
            }
            LinkedHashMap<String, Class> entries = new LinkedHashMap<>();
            addEntries(entries, definitionsByType, normalTypes);
            addEntries(entries, definitionsByType, overrideTypes);
            return Collections.unmodifiableMap(entries);
        } catch (Exception e) {
            throw new IllegalStateException(Messages.parseFile(path), e);
        }
    }

    private static boolean available(Extension extension, ClassLoader classLoader) {
        String[] classes = extension.onClass();
        if (classes.length == 0) {
            return true;
        }
        try {
            for (String type : classes) {
                classLoader.loadClass(type);
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void addEntries(Map<String, Class> entries, Map<Class<?>, Extension> definitions, Set<Class<?>> types) {
        List<Class<?>> sorted = new ArrayList<>(types);
        sorted.sort(Comparator.comparingInt(type -> definitions.get(type).order()));
        for (Class<?> type : sorted) {
            Extension extension = definitions.get(type);
            for (String name : StringUtil.deduplicate(extension.value())) {
                entries.putIfAbsent(name, type);
            }
        }
    }

    /**
     * Returns the extension interface type.
     */
    public Class<T> type() {
        return type;
    }

    /**
     * Returns whether extensions are loaded lazily.
     */
    public boolean lazyLoaded() {
        return lazyLoaded;
    }

    /**
     * Returns the scoped context that owns this loader.
     */
    public ScopedContext scopedContext() {
        return scopedContext;
    }

    /**
     * Returns the component descriptor for the extension type.
     */
    public ComponentDescriptor componentDescriptor() {
        return descriptor;
    }

    /**
     * Returns the preferred extension, falling back to the primary extension.
     *
     * @param extensionName preferred extension name
     * @return resolved extension instance
     */
    public T preferredExtension(String extensionName) {
        extensionName = StringUtil.isBlank(extensionName) ? primaryExtension : extensionName;
        if (StringUtil.isBlank(extensionName)) {
            throw new IllegalStateException("Attempted to use primary extension, but none was found for '" + type.getTypeName() + "'.");
        }
        ExtensionEntry<T> entry = extensionEntries.get(extensionName);
        if (entry != null) return entry.extension();
        if (!extensionName.equals(primaryExtension) && StringUtil.isNotBlank(primaryExtension)) {
            ExtensionEntry<T> primaryEntry = extensionEntries.get(primaryExtension);
            if (primaryEntry != null) {
                return primaryEntry.extension();
            }
        }
        throw new IllegalStateException(format("Extension '{}' not found, and primary extension also not found for '{}'.", extensionName, type.getTypeName()));
    }

    /**
     * Returns the primary extension instance.
     */
    public T primaryExtension() {
        return namedExtension(primaryExtension);
    }

    /**
     * Returns the named extension instance.
     *
     * @param extensionName extension name
     * @return extension instance
     */
    public T namedExtension(String extensionName) {
        AssertUtil.notBlank(extensionName, "extensionName");
        ExtensionEntry<T> entry = extensionEntries.get(extensionName);
        if (entry == null) {
            throw new IllegalStateException(format("Failed to load extension '{}' for '{}': not found or not eligible for loading.", extensionName, type.getTypeName()));
        }
        return entry.extension();
    }

    /**
     * Returns extension instances matching the supplied filter.
     *
     * @param filter extension filter
     * @return matching extension instances
     */
    public Collection<T> extensions(BiPredicate<String, ExtensionEntry<T>> filter) {
        return namedExtensions(filter).values();
    }

    /**
     * Returns named extension instances matching the supplied filter.
     *
     * @param filter extension filter
     * @return matching extension instances by name
     */
    public Map<String, T> namedExtensions(BiPredicate<String, ExtensionEntry<T>> filter) {
        return CollectionUtil.unmodifiable(extensionEntries, filter, ExtensionEntry::extension);
    }

    @Override
    public void clear() {
        extensionEntries.values().forEach(ExtensionEntry::clear);
        extensionEntries.clear();
    }

    @Override
    public String toString() {
        return ObjectUtil.simpleClassName(this) + "<" + type.getSimpleName() + ">";
    }

    ExtensionLoader<T> load() {
        if (loaded || lazyLoaded) {
            return this;
        }
        synchronized (this) {
            if (loaded || loading) {
                return this;
            }
            loading = true;
            try {
                for (ExtensionEntry<T> entry : new LinkedHashSet<>(extensionEntries.values())) {
                    if (entry.singleton()) {
                        entry.extension();
                    }
                }
                loaded = true;
            } finally {
                loading = false;
            }
        }
        return this;
    }
}
