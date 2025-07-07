package io.effi.rpc.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Immutable key based on String array content.
 */
public final class StringArrayKey implements Comparable<StringArrayKey>, Serializable {

    private final String[] elements;
    private final int hash;

    private StringArrayKey(String[] elements) {
        this.elements = AssertUtil.notNull(elements, "elements").clone();
        this.hash = Arrays.hashCode(this.elements);
    }

    public static StringArrayKey of(String... elements) {
        return new StringArrayKey(elements);
    }

    public static StringArrayKey of(Collection<String> elements) {
        return new StringArrayKey(elements.toArray(StringUtil.emptyArray()));
    }

    public String[] elements() {
        return elements.clone();
    }

    public int length() {
        return elements.length;
    }

    public String get(int index) {
        return elements[index];
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StringArrayKey other)) return false;
        return Arrays.equals(this.elements, other.elements);
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    @Override
    public int compareTo(StringArrayKey other) {
        int len1 = this.elements.length;
        int len2 = other.elements.length;
        int lim = Math.min(len1, len2);
        for (int i = 0; i < lim; i++) {
            String s1 = this.elements[i];
            String s2 = other.elements[i];
            if (Objects.equals(s1, s2)) continue;
            if (s1 == null) return -1;
            if (s2 == null) return 1;
            int cmp = s1.compareTo(s2);
            if (cmp != 0) return cmp;
        }
        return Integer.compare(len1, len2);
    }
}
