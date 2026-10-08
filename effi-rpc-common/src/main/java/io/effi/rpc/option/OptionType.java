package io.effi.rpc.option;

/**
 * Describes the runtime behavior of an option value.
 *
 * @param <T> the option value type
 */
public interface OptionType<T> {

    /**
     * Converts a raw value to the typed option value.
     *
     * @param value raw option value
     * @return converted option value
     */
    T convert(Object value);

    /**
     * Creates a current-scope option name without a default value.
     *
     * @param name option name
     * @return typed option name
     */
    default OptionName<T> onlyCurrent(String name) {
        return name(name, OptionStrategy.ONLY_CURRENT);
    }

    /**
     * Creates an option name backed by this type without a default value.
     *
     * @param name     option name
     * @param strategy option resolution strategy
     * @return typed option name
     */
    default OptionName<T> name(String name, OptionStrategy strategy) {
        return name(name, strategy, null);
    }

    /**
     * Creates an option name backed by this type.
     *
     * @param name         option name
     * @param strategy     option resolution strategy
     * @param defaultValue default option value
     * @return typed option name
     */
    default OptionName<T> name(String name, OptionStrategy strategy, T defaultValue) {
        return new DefaultOptionName<>(name, strategy, defaultValue, this);
    }

    /**
     * Creates a current-scope option name.
     *
     * @param name         option name
     * @param defaultValue default option value
     * @return typed option name
     */
    default OptionName<T> onlyCurrent(String name, T defaultValue) {
        return name(name, OptionStrategy.ONLY_CURRENT, defaultValue);
    }

    /**
     * Creates a current-first option name without a default value.
     *
     * @param name option name
     * @return typed option name
     */
    default OptionName<T> currentFirst(String name) {
        return name(name, OptionStrategy.CURRENT_FIRST);
    }

    /**
     * Creates a current-first option name.
     *
     * @param name         option name
     * @param defaultValue default option value
     * @return typed option name
     */
    default OptionName<T> currentFirst(String name, T defaultValue) {
        return name(name, OptionStrategy.CURRENT_FIRST, defaultValue);
    }

    /**
     * Creates a parent-first option name without a default value.
     *
     * @param name option name
     * @return typed option name
     */
    default OptionName<T> parentFirst(String name) {
        return name(name, OptionStrategy.PARENT_FIRST);
    }

    /**
     * Creates a parent-first option name.
     *
     * @param name         option name
     * @param defaultValue default option value
     * @return typed option name
     */
    default OptionName<T> parentFirst(String name, T defaultValue) {
        return name(name, OptionStrategy.PARENT_FIRST, defaultValue);
    }
}
