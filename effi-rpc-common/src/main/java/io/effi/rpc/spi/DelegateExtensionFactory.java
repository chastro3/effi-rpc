package io.effi.rpc.spi;

import io.effi.rpc.util.Ordered;
import io.effi.rpc.util.ReflectionUtil;

import java.util.*;

/**
 * Delegate implementation of {@link ExtensionFactory}.
 */
public class DelegateExtensionFactory implements ExtensionFactory {

    private final List<ExtensionFactory> factories = new ArrayList<>(4);

    public DelegateExtensionFactory() {
        add(new ReflectionExtensionFactory());
    }

    public void add(ExtensionFactory factory) {
        if (factory != null) {
            int index = Collections.binarySearch(factories, factory, Comparator.comparingInt(ExtensionFactory::order));
            if (index < 0) {
                index = -index - 1;
            }
            factories.add(index, factory);
        }
    }

    @Override
    public <T> T getExtension(Class<T> type, List<String> names) {
        return factories.stream()
                .map(injector -> injector.getExtension(type, names))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Reflection implementation of {@link ExtensionFactory}.
     */
    public static class ReflectionExtensionFactory implements ExtensionFactory {
        @Override
        public <T> T getExtension(Class<T> type, List<String> names) {
            return ReflectionUtil.newInstance(type);
        }

        @Override
        public int order() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
