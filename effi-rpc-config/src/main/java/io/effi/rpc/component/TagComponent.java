package io.effi.rpc.component;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extends SingleComponent to support tagging functionalities.
 */
public class TagComponent<T> extends SingleComponent<T> {

    private volatile Set<String> tags;

    public TagComponent(String key, T component, ScopedComponentRepository owner) {
        super(key, component, owner);
    }

    public TagComponent<T> addTag(String... tags) {
        if (CollectionUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                if (StringUtil.isNotBlank(tag)) {
                    delayedTags().add(tag);
                }
            }
        }
        return this;
    }

    public boolean hasTags(String... tags) {
        if (CollectionUtil.isEmpty(tags)) return true;
        if (CollectionUtil.isEmpty(this.tags)) return false;
        for (String tag : tags) {
            if (!this.tags.contains(tag)) return false;
        }
        return true;
    }


    public Set<String> tags() {
        return tags == null ? Collections.emptySet() : Collections.unmodifiableSet(tags);
    }

    private Set<String> delayedTags() {
        if (tags == null) {
            synchronized (key().intern()) {
                if (tags == null) {
                    tags = new HashSet<>();
                }
            }
        }

        return tags;
    }
}
