package io.effi.rpc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides common collection operations.
 */
@SuppressWarnings("unchecked")
public final class CollectionUtil {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];


    public static <T, R> Collection<R> unmodifiable(Collection<T> source, Predicate<T> predicate, Function<T, R> mapper) {
        if (isEmpty(source)) {
            return Collections.emptyList();
        }
        List<R> result = new ArrayList<>();
        for (T item : source) {
            if (predicate.test(item)) {
                result.add(mapper.apply(item));
            }
        }
        if (isEmpty(result)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(result);
    }

    public static <T, R> Map<String, R> unmodifiable(Map<String, T> source, Predicate<T> predicate, Function<T, R> mapper) {
        if (isEmpty(source)) {
            return Collections.emptyMap();
        }
        Map<String, R> result = new LinkedHashMap<>();
        for (Map.Entry<String, T> entry : source.entrySet()) {
            T value = entry.getValue();
            if (predicate.test(value)) {
                result.put(entry.getKey(), mapper.apply(value));
            }
        }
        if (isEmpty(result)) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Adds unique elements to the list.
     */
    public static <E> void addUnique(List<E> list, E... elements) {
        addUnique(list, Arrays.asList(elements));
    }

    public static <E> void addUnique(List<E> list, Collection<E> elements) {
        if (isEmpty(elements)) return;
        Set<E> set = new HashSet<>();
        for (E element : elements) {
            if (set.add(element)) {
                list.add(element);
            }
        }
    }

    public static String[] deduplicate(String[] array) {
        final int n = array.length;
        if (n <= 1) return array;
        LinkedHashSet<String> seen = new LinkedHashSet<>(n);
        for (int i = 0; i < n; i++) {
            seen.add(array[i]);
        }
        return seen.toArray(EMPTY_STRING_ARRAY);
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
     * Replaces elements in the old list with those from the new list when matched.
     */
    public static <T> void replaceIfMatch(List<T> oldList, List<T> newList, BiPredicate<T, T> matcher) {
        if (CollectionUtil.isEmpty(oldList)) {
            oldList.addAll(newList);
        } else {
            for (int i = 0; i < oldList.size(); i++) {
                T oldElement = oldList.get(i);
                for (T newElement : newList) {
                    if (matcher.test(oldElement, newElement)) {
                        oldList.set(i, newElement);
                        break;
                    }
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


    private CollectionUtil() {
    }
}
