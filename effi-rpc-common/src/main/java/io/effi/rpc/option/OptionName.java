package io.effi.rpc.option;

/**
 * Defines a typed option name and its resolution behavior.
 *
 * @param <T> the option value type
 */
public interface OptionName<T> {

    /**
     * Returns the option name.
     */
    String name();

    /**
     * Returns the option resolution strategy.
     */
    OptionStrategy strategy();

    /**
     * Returns the default option value.
     */
    T defaultValue();

    /**
     * Returns the option value type.
     */
    OptionType<T> type();
}
