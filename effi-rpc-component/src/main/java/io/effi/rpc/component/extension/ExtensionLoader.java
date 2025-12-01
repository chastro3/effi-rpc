package io.effi.rpc.component.extension;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ComponentDescriptor;
import io.effi.rpc.component.ScopedContext;
import io.effi.rpc.constant.ResourcePaths;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.trait.Cleanable;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.trait.Ordered;
import io.effi.rpc.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

import static io.effi.rpc.util.StringUtil.format;

/**
 * Loads and manages extension components of a specified type.
 * <p>
 * Scans, loads, and instantiates implementation classes annotated with {@link Extension},
 * managing them according to their configuration, including scope, conditional filtering,
 * and priority ordering.
 * <p>
 * Supports retrieving extension instances by id, obtaining the default extension,
 * and listing all instances that meet specified conditions.
 *
 * @param <T> the extension type, which must be an interface annotated with {@link Extensible}
 */
public final class ExtensionLoader<T> implements Cleanable {

    private static final Map<Class<?>, ExtensionEntry<?>> EXTENSION_ENTRIES = new ConcurrentHashMap<>();

    private final Map<String, ExtensionEntry<T>> extensionEntries;

    private final Extensible extensible;

    private final ScopedContext scopedContext;

    private final ComponentDescriptor descriptor;

    private final Class<T> type;

    private final String primaryExtension;

    private final boolean lazyLoaded;

    ExtensionLoader(ScopedContext scopedContext, Class<T> type, ComponentDescriptor descriptor) {
        this.extensible = ensureExtensible(type);
        this.scopedContext = scopedContext;
        this.type = type;
        this.descriptor = descriptor;
        this.lazyLoaded = extensible.lazyLoad();
        this.extensionEntries = loadExtensionEntries(type);
        this.primaryExtension = findPrimaryExtension();
    }

    public Class<T> type() {
        return type;
    }

    public boolean LazyLoaded() {
        return lazyLoaded;
    }

    public ScopedContext scopedContext() {
        return scopedContext;
    }

    public ComponentDescriptor componentDescriptor() {
        return descriptor;
    }

    public T namedExtension(String extensionName) {
        AssertUtil.notBlank(extensionName, "extensionName");
        ExtensionEntry<T> entry = extensionEntries.get(extensionName);
        if (entry == null) {
            throw new IllegalStateException(format("Failed to load extension '{}' for '{}': not found or not eligible for loading.", extensionName, type.getTypeName()));
        }
        return entry.extension();
    }

    public T preferredExtension(String extensionName) {
        extensionName = StringUtil.isBlank(extensionName) ? primaryExtension : extensionName;
        if (StringUtil.isBlank(extensionName)) {
            throw new IllegalStateException("Attempted to use primary extension, but none was found for '" + type.getTypeName() + "'.");
        }
        ExtensionEntry<T> entry = extensionEntries.get(extensionName);
        if (entry != null) return entry.extension();
        if (!extensionName.equals(primaryExtension)
                && StringUtil.isNotBlank(primaryExtension)) {
            ExtensionEntry<T> primaryEntry = extensionEntries.get(primaryExtension);
            if (primaryEntry != null) {
                return primaryEntry.extension();
            }
        }
        throw new IllegalStateException(format("Extension '{}' not found, and primary extension also not found for '{}'.", extensionName, type.getTypeName()));
    }

    public T primaryExtension() {
        return namedExtension(primaryExtension);
    }

    public Collection<T> extensions(BiPredicate<String, ExtensionEntry<T>> filter) {
        return namedExtensions(filter).values();
    }

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

    private Extensible ensureExtensible(Class<?> type) {
        if (!type.isInterface())
            throw new IllegalArgumentException("Extension type '" + type.getName() + "' must be an interface");
        return AssertUtil.requireAnnotation(type, Extensible.class);
    }

    /**
     * Loads all available extensions for the specified type by scanning resources.
     */
    @SuppressWarnings("unchecked")
    private Map<String, ExtensionEntry<T>> loadExtensionEntries(Class<T> type) {
        String path = ResourcePaths.SPI_SERVICES_DIR + type.getTypeName();
        try {
            ClassLoader classLoader = ClassUtil.findClassLoader(type);
            Enumeration<URL> resources = classLoader.getResources(path);
            List<ExtensionEntry<T>> extensionEntries = new ArrayList<>();
            List<ExtensionEntry<T>> overrideExtensionEntries = new ArrayList<>();
            // Load and create ExtensionEntry instance for all valid extensions
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String extensionClassName;
                    while ((extensionClassName = br.readLine()) != null) {
                        Class<?> extensionClass = classLoader.loadClass(extensionClassName);
                        Extension extension = extensionClass.getAnnotation(Extension.class);
                        if (type.isAssignableFrom(extensionClass) && extension != null) {
                            ExtensionEntry<T> extensionEntry = (ExtensionEntry<T>) EXTENSION_ENTRIES.computeIfAbsent(extensionClass, k -> new ExtensionEntry<>(this, (Class<? extends T>) extensionClass, extension));
                            if (extensionEntry.available()) {
                                extensionEntries.add(extensionEntry);
                                if (extensionEntry.canOverride()) {
                                    overrideExtensionEntries.add(extensionEntry);
                                }
                            }
                        }
                    }
                }
            }
            LinkedHashMap<String, ExtensionEntry<T>> result = new LinkedHashMap<>();
            addEntries(result, extensionEntries);
            addEntries(result, overrideExtensionEntries);
            return result.isEmpty() ? Collections.emptyMap() : result;
        } catch (Exception e) {
            throw new IllegalStateException(Messages.parseFile(path), e);
        }
    }

    private void addEntries(LinkedHashMap<String, ExtensionEntry<T>> entriesMap, List<ExtensionEntry<T>> entries) {
        List<ExtensionEntry<T>> sortedEntries = Ordered.sort(entries);
        for (ExtensionEntry<T> entry : sortedEntries) {
            for (String name : entry.names()) {
                entriesMap.putIfAbsent(name, entry);
            }
        }
    }

    private String findPrimaryExtension() {
        String primaryExtension = extensible.value();
        if (StringUtil.isNotBlank(primaryExtension)) return primaryExtension;
        for (Map.Entry<String, ExtensionEntry<T>> mapEntry : extensionEntries.entrySet()) {
            ExtensionEntry<T> entry = mapEntry.getValue();
            if (entry.primary()) return mapEntry.getKey();
        }
        return null;
    }
}
