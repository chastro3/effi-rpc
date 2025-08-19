package io.effi.rpc.component;

import io.effi.rpc.util.CollectionUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages dynamic tags with thread-safe addition and retrieval.
 * <p>
 * Provides mutable tag management capabilities with lazy initialization
 * and thread-safe operations for adding and accessing component tags.
 */
public class DynamicTagComponent implements TagComponent {

    private final Object lock = new Object();

    protected volatile Set<String> tags;

    public DynamicTagComponent addTags(String... tags) {
        if (CollectionUtil.isNotEmpty(tags)) {
            delayedTags().addAll(Arrays.asList(tags));
        }
        return this;
    }

    protected Set<String> delayedTags() {
        Set<String> result = tags;
        if (result != null) return result;
        synchronized (lock) {
            result = tags;
            return result != null ? result : (tags = new HashSet<>());
        }
    }

    @Override
    public Set<String> tags() {
        return tags;
    }
}
