package io.effi.rpc.option;

import io.effi.rpc.trait.FluentBuilder;

import java.util.Map;

/**
 * Stores and resolves typed options.
 */
public interface Options {

    static Options empty() {
        return DefaultOptions.empty();
    }

    static Options create() {
        return new DefaultOptions();
    }

    static Options create(int initialCapacity) {
        return new DefaultOptions(initialCapacity);
    }

    static Options create(Map<String, Object> items) {
        return new DefaultOptions(items);
    }

    /**
     * Adds a typed option value.
     *
     * @param name  option name
     * @param value option value
     * @return current options instance
     */
    <V> Options addOption(OptionName<V> name, V value);

    /**
     * Removes and returns the current value for the given option name.
     *
     * @param name option name
     * @return removed option value
     */
    <V> V removeOption(OptionName<V> name);

    /**
     * Returns an unmodifiable view of all option values.
     */
    Map<String, Object> items();

    /**
     * Returns the option value when present, otherwise the supplied default value.
     *
     * @param name         option name
     * @param defaultValue fallback value
     * @return resolved option value
     */
    default <V> V option(OptionName<V> name, V defaultValue) {
        V value = option(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Returns the resolved value for the given option name.
     *
     * @param name option name
     * @return resolved option value
     */
    <V> V option(OptionName<V> name);

    /**
     * Supplies access to an options instance.
     */
    interface Supplier extends Options {

        @Override
        default <V> Supplier addOption(OptionName<V> name, V value) {
            options().addOption(name, value);
            return this;
        }

        /**
         * Returns the associated options instance.
         */
        Options options();

        @Override
        default <V> V option(OptionName<V> name) {
            return options().option(name);
        }

        @Override
        default <V> V removeOption(OptionName<V> name) {
            return options().removeOption(name);
        }

        @Override
        default Map<String, Object> items() {
            return options().items();
        }
    }

    /**
     * Builds an options-aware instance with fluent method chaining.
     *
     * @param <T>    built instance type
     * @param <SELF> concrete builder type
     */
    interface Builder<T, SELF extends Builder<T, SELF>>
            extends FluentBuilder<T, SELF>, Supplier {

        @Override
        default <V> SELF addOption(OptionName<V> name, V value) {
            options().addOption(name, value);
            return self();
        }

    }
}
