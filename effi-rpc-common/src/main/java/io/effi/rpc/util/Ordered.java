package io.effi.rpc.util;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Define objects with order values for precedence.
 * Lower values indicate higher priority. Equal values have arbitrary order.
 */
public interface Ordered {

    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    int DEFAULT = 0;

    /**
     * Sort {@link Ordered} objects by order values.
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



