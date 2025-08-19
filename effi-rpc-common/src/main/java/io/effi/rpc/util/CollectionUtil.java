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
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides common collection operations.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class CollectionUtil {

    private static final Map.Entry[] EMPTY_ENTRY_ARRAY = new Map.Entry[0];

    /**
     * Returns an empty entry array.
     */
    public static <K, V> Map.Entry<K, V>[] emptyEntryArray() {
        return EMPTY_ENTRY_ARRAY;
    }

    /**
     * Replaces the key in a map when the key (e.g. name) of the value object changes.
     */
    public static <K, V> boolean replaceMapKey(Map<K, V> map, K oldKey, K newKey,V value) {
        if (Objects.equals(oldKey, newKey)) {
            return false; // no change needed
        }
        if (oldKey != null) value = map.remove(oldKey);
            map.put(newKey, value);
            return true;

    }

    public static <T extends Comparable> Collection<T> flatDistinctCollection(Collection<? extends Iterable<T>> collection) {
        if (isEmpty(collection)) return Collections.emptySet();
        TreeSet<T> result = new TreeSet<>();
        for (Iterable<T> iterable : collection) {
            if (iterable != null) {
                for (T item : iterable) {
                    result.add(item);
                }
            }
        }
        return result.isEmpty() ? Collections.emptySet() : result;
    }

    public static <T extends Comparable> Collection<T> flatDistinctArray(Collection<? extends T[]> collection) {
        if (isEmpty(collection)) return Collections.emptySet();
        TreeSet<T> result = new TreeSet<>();
        for (T[] array : collection) {
            if (array != null) {
                for (T item : array) {
                    result.add(item);
                }
            }
        }
        return result.isEmpty() ? Collections.emptySet() : result;
    }


    /**
     * Converts an array to a hash set.
     */
    public static <T> Set<T> toHashSet(T[] array) {
        if (isEmpty(array)) {
            return Collections.emptySet();
        }
        if (array.length == 1) {
            return Collections.singleton(array[0]);
        }
        Set<T> set;
        if (array.length <= 20) {
            set = new HashSet<>();
        } else {
            int capacity = (int) (array.length / 0.75f) + 1;
            set = new HashSet<>(capacity);
        }
        for (T s : array) {
            if (s != null) {
                set.add(s);
            }
        }
        return set;
    }

    /**
     * Converts an array to a linked hash set.
     */
    public static <T> Set<T> toLinkedHashSet(T[] array) {
        if (isEmpty(array)) {
            return Collections.emptySet();
        }
        if (array.length == 1) {
            return Collections.singleton(array[0]);
        }
        Set<T> set;
        if (array.length <= 20) {
            set = new LinkedHashSet<>();
        } else {
            int capacity = (int) (array.length / 0.75f) + 1;
            set = new LinkedHashSet<>(capacity);
        }
        for (T s : array) {
            if (s != null) {
                set.add(s);
            }
        }
        return set;
    }

    /**
     * Returns an unmodifiable view of a filtered and mapped collection.
     */
    public static <T, R> Collection<R> unmodifiable(Collection<T> source, Predicate<T> predicate, Function<T, R> mapper) {
        if (isEmpty(source)) {
            return Collections.emptyList();
        }
        List<R> result = new ArrayList<>();
        for (T item : source) {
            if (predicate != null) {
                if (predicate.test(item)) {
                    result.add(mapper.apply(item));
                }
            } else {
                result.add(mapper.apply(item));
            }
        }
        if (isEmpty(result)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable view of a filtered map.
     */
    public static <T> Map<String, T> unmodifiable(Map<String, T> source, BiPredicate<String, T> predicate) {
        if (isEmpty(source) || predicate == null) {
            return source;
        }
        Map<String, T> result = new LinkedHashMap<>();
        for (Map.Entry<String, T> entry : source.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue();
            if (predicate.test(key, value)) {
                result.put(key, value);
            }
        }
        if (isEmpty(result)) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns an unmodifiable view of a filtered and mapped map.
     */
    public static <T, R> Map<String, R> unmodifiable(Map<String, T> source, BiPredicate<String, T> predicate, Function<T, R> mapper) {
        if (isEmpty(source)) {
            return Collections.emptyMap();
        }
        Map<String, R> result = new LinkedHashMap<>();
        for (Map.Entry<String, T> entry : source.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue();
            if (predicate != null) {
                if (predicate.test(key, value)) {
                    result.put(key, mapper.apply(value));
                }
            } else {
                result.put(key, mapper.apply(value));
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

    /**
     * Adds unique elements to the list.
     */
    public static <E> void addUnique(List<E> list, Collection<E> elements) {
        if (isEmpty(elements)) return;
        Set<E> set = new HashSet<>();
        for (E element : elements) {
            if (set.add(element)) {
                list.add(element);
            }
        }
    }


    /**
     * Merges multiple collections into a single set.
     * <p>
     * Duplicates are eliminated. Returns an empty list if input is empty.
     */
    @SafeVarargs
    public static <E> Collection<E> merge(Collection<E>... collections) {
        if (isEmpty(collections)) return Collections.emptyList();
        Set<E> set = new HashSet<>();
        for (Collection<E> collection : collections) {
            if (CollectionUtil.isNotEmpty(collection)) set.addAll(collection);
        }
        return set.isEmpty() ? Collections.emptyList() : set;
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
