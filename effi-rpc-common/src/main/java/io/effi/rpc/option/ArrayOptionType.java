package io.effi.rpc.option;

/**
 * Describes an array option type that supports parent merging.
 *
 * @param <A> the array type, such as {@code String[]}, {@code int[]}, or
 *            {@code long[]}
 */
public interface ArrayOptionType<A> extends OptionType<A> {

    /**
     * Creates an option name that merges its parent and current arrays.
     *
     * @param name option name
     * @return typed option name
     */
    default OptionName<A> mergeParent(String name) {
        return name(name, OptionStrategy.MERGE_PARENT);
    }

    /**
     * Creates an option name that merges its parent and current arrays.
     *
     * @param name option name
     * @param defaultValue default option value
     * @return typed option name
     */
    default OptionName<A> mergeParent(String name, A defaultValue) {
        return name(name, OptionStrategy.MERGE_PARENT, defaultValue);
    }

    /**
     * Merges the resolved parent array with the current array.
     *
     * @param parent resolved parent array
     * @param current current array
     * @return merged array
     */
    A merge(A parent, A current);
}
