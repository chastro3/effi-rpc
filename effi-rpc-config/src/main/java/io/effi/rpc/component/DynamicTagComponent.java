package io.effi.rpc.component;

import io.effi.rpc.util.CollectionUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages dynamic tags with thread-safe addition and retrieval.
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
        if (tags == null) {
            synchronized (lock) {
                if (tags == null) {
                    tags = new HashSet<>();
                }
            }
        }
        return tags;
    }

    @Override
    public Set<String> tags() {
        return tags;
    }
}
