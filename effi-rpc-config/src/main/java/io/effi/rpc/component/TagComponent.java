package io.effi.rpc.component;

import io.effi.rpc.util.CollectionUtil;

import java.util.Collections;
import java.util.Set;

/**
 * Provides tag management capabilities for components.
 */
public interface TagComponent {

    /**
     * Checks if the component contains all specified tags.
     *
     * @param tags tags to check
     * @return true if all tags exist or if no tags specified
     */
    default boolean hasTags(String... tags) {
        if (CollectionUtil.isEmpty(tags)) return true;
        Set<String> existedTags = tags();
        if (CollectionUtil.isEmpty(existedTags)) return false;
        for (String tag : tags) {
            if (!existedTags.contains(tag)) return false;
        }
        return true;
    }

    /**
     * Returns the set of tags associated with the component.
     */
    default Set<String> tags() {
        return Collections.emptySet();
    }
}

