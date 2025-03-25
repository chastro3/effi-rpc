package io.effi.rpc.common.extension;

import io.effi.rpc.common.util.CollectionUtil;

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
     * <p>
     * This value assigns the highest priority, ensuring execution before others
     * with higher order values.
     *
     * @see Integer#MIN_VALUE
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * Constant for the default order value, used when no specific order is needed.
     */
    int DEFAULT = 0;

    /**
     * Constant for the lowest precedence value.
     * <p>
     * This value assigns the lowest priority, ensuring execution after others
     * with lower order values.
     *
     * @see Integer#MAX_VALUE
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * Sorts a list of {@link Ordered} objects based on their order values in ascending order.
     * <p>
     * If the input list is empty or null, the original list is returned.
     *
     * @param <T>    the type of objects, which must extend {@link Ordered}
     * @param values the list to sort
     * @return a sorted list of objects
     */
    static <T extends Ordered> List<T> order(List<T> values) {
        if (CollectionUtil.isEmpty(values)) {
            return values;
        }
        return values.stream()
                .sorted(Comparator.comparing(Ordered::getOrder))
                .collect(Collectors.toList());
    }

    /**
     * Returns the order value of this object, with a lower value indicating higher priority.
     * <p>
     * The default order value is 0.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    default int getOrder() {
        return DEFAULT;
    }

}


