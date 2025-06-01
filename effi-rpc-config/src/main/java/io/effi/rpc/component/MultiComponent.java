package io.effi.rpc.component;

import io.effi.rpc.util.CollectionUtil;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides a string-keyed implementation of {@link ComponentRepository}.
 */
public class MultiComponent<T> extends AbstractComponentRepository<String, TagComponent<T>> {

    public Collection<TagComponent<T>> listOf(String... tags) {
        return CollectionUtil.unmodifiable(
                components.values(),
                item -> item.hasTags(tags),
                Function.identity()
        );
    }

    public Collection<T> listValueOf(String... tags) {
        return CollectionUtil.unmodifiable(
                components.values(),
                item -> item.hasTags(tags),
                SingleComponent::component
        );
    }

    public Map<String, TagComponent<T>> mapOf(String... tags) {
        return CollectionUtil.unmodifiable(
                components,
                item -> item.hasTags(tags),
                Function.identity()
        );
    }

    public Map<String, T> mapValueOf(String... tags) {
        return CollectionUtil.unmodifiable(
                components,
                item -> item.hasTags(tags),
                SingleComponent::component
        );
    }
}
