package io.effi.rpc.util;

import java.util.Objects;

/**
 * Immutable pair that can be used as a key in maps or sets.
 */
public final class Pair<L, R> implements Comparable<Pair<L, R>> {

    private final L left;
    private final R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public L left() {
        return left;
    }

    public R right() {
        return right;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pair<?, ?> pair)) return false;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(left) ^ Objects.hashCode(right);
    }

    @Override
    public int compareTo(Pair<L, R> other) {
        int leftCmp = compare(left, other.left);
        if (leftCmp != 0) return leftCmp;
        return compare(right, other.right);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int compare(Object o1, Object o2) {
        if (o1 == o2) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        return ((Comparable) o1).compareTo(o2);
    }

    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}
