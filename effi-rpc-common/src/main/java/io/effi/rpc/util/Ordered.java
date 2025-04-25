package io.effi.rpc.util;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Objects with an order value to determine their precedence in a sequence,
 * typically used for filters or handlers.
 * <p>
 * Lower order values indicate higher priority. Objects with the same order value
 * have an arbitrary relative order.
 */
public interface Ordered {

    /**
     * Constant for the highest precedence value.
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * Constant for the default order value, used when no specific order is needed.
     */
    int DEFAULT = 0;

    /**
     * Constant for the lowest precedence value.
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * Sorts a list of {@link Ordered} objects based on their order values in ascending order.
     *
     * @param <T>    the type of objects, which must extend {@link Ordered}
     * @param values the list to sort
     * @return a sorted list of objects
     */
    static <T extends Ordered> List<T> sort(List<T> values) {
        if (CollectionUtil.isEmpty(values)) {
            return values;
        }
        return values.stream()
                .sorted(Comparator.comparing(Ordered::order))
                .collect(Collectors.toList());
    }

    default int order() {
        return DEFAULT;
    }

}


