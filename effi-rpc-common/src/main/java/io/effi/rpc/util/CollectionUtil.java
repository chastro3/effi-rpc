package io.effi.rpc.util;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Provides common collection operations.
 */
@SuppressWarnings("unchecked")
public final class CollectionUtil {

    private CollectionUtil() {
    }

    /**
     * Adds unique elements to the list.
     */
    public static <E> void addUnique(List<E> list, E... elements) {
        Set<E> set = new HashSet<>(list);
        for (E element : elements) {
            if (set.add(element)) {
                list.add(element);
            }
        }
    }

    /**
     * Adds items to a collection based on the provided predicate.
     */
    public static <T> void addToList(Collection<T> collection, BiPredicate<T, T> predicate, T... items) {
        addToList(collection, predicate, null, items);
    }

    /**
     * Adds items to a collection based on the provided predicate and invokes a callback on successful addition.
     */
    public static <T> void addToList(Collection<T> collection, BiPredicate<T, T> predicate, Consumer<T> successCallBack, T... items) {
        if (items != null && items.length > 0) {
            loop:
            for (T item : items) {
                for (T collect : collection) {
                    if (predicate.test(collect, item)) {
                        continue loop;
                    }
                }
                collection.add(item);
                if (successCallBack != null) {
                    successCallBack.accept(item);
                }
            }
        }
    }

    /**
     * Checks if a collection is empty or null.
     */
    public static boolean isEmpty(Collection<?> value) {
        return Objects.isNull(value) || value.isEmpty();
    }

    /**
     * Checks if a map is empty or null.
     */
    public static boolean isEmpty(Map<?, ?> value) {
        return Objects.isNull(value) || value.isEmpty();
    }

    /**
     * Checks if an array is empty or null.
     */
    public static boolean isEmpty(Object[] value) {
        return value == null || value.length == 0;
    }

    /**
     * Checks if a collection is not empty and not null.
     */
    public static boolean isNotEmpty(Collection<?> value) {
        return !isEmpty(value);
    }

    /**
     * Checks if a map is not empty and not null.
     */
    public static boolean isNotEmpty(Map<?, ?> value) {
        return !isEmpty(value);
    }

    /**
     * Checks if an array is not empty and not null.
     */
    public static boolean isNotEmpty(Object[] value) {
        return !isEmpty(value);
    }

}
