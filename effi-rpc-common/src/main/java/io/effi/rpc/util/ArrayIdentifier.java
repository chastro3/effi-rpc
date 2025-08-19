package io.effi.rpc.util;

import java.util.Arrays;

public final class ArrayIdentifier<T> implements Comparable<ArrayIdentifier<T>> {

    private static final ArrayIdentifier<?> EMPTY = new ArrayIdentifier<>(new Object[0]);

    private final T[] elements;
    private final int hash;

    private ArrayIdentifier(T[] elements) {
        this.elements = elements;
        this.hash = computeHash(elements);
    }

    @SafeVarargs
    public static <T> ArrayIdentifier<T> of(T... elements) {
        if (CollectionUtil.isEmpty(elements)) return empty();
        return new ArrayIdentifier<>(elements);
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayIdentifier<T> empty() {
        return (ArrayIdentifier<T>) EMPTY;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArrayIdentifier<?> other)) return false;
        return Arrays.equals(this.elements, other.elements);
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    @Override
    public int compareTo(ArrayIdentifier<T> other) {
        T[] thisElements = elements;
        T[] otherElements = other.elements;
        int len1 = thisElements.length;
        int len2 = otherElements.length;
        int lim = Math.min(len1, len2);
        for (int i = 0; i < lim; i++) {
            T s1 = thisElements[i];
            T s2 = otherElements[i];
            if (s1 == s2) continue;
            if (s1 == null) return -1;
            if (s2 == null) return 1;
            if (s1 instanceof Comparable) {
                @SuppressWarnings("unchecked")
                int cmp = ((Comparable<T>) s1).compareTo(s2);
                if (cmp != 0) return cmp;
            }
        }
        return Integer.compare(len1, len2);
    }

    private int computeHash(T[] elements) {
        if (elements == null) return 0;
        int h = 1;
        for (T element : elements) {
            h = 31 * h + (element == null ? 0 : element.hashCode());
        }
        return h;
    }

}
