package io.effi.rpc.config;

import io.effi.rpc.trait.FluentBuilder;

import java.util.Map;

/**
 * Manages options for retrieval and update.
 * <p>
 * Provides a centralized interface for setting, getting, and removing options
 * by name or typed option name, with support for default values.
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
     * Sets the value by option name.
     */
    <V> Options addOption(OptionName<V> name, V value);

    /**
     * Sets the value by name.
     */
    <V> Options addOption(String name, V value);

    /**
     * Retrieves the value by option name.
     */
    <V> V option(OptionName<V> name);

    /**
     * Retrieves the value by name.
     */
    <V> V option(String name);

    /**
     * Removes the value by name.
     */
    <V> V removeOption(OptionName<V> name);

    /**
     * Removes the value by name.
     */
    <V> V removeOption(String name);

    /**
     * Returns all config entries.
     */
    Map<String, Object> items();


    default <V> V option(OptionName<V> name, V defaultValue) {
        V value = option(name);
        return value != null ? value : defaultValue;
    }

    default <V> V option(String name, V defaultValue) {
        V value = option(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Supplies access to the {@link Options}.
     */
    interface Supplier extends Options {

        /**
         * Returns the associated {@link Options}.
         */
        Options options();

        @Override
        default <V> Supplier addOption(OptionName<V> name, V value) {
            options().addOption(name.name(), value);
            return this;
        }

        @Override
        default <V> Supplier addOption(String name, V value) {
            options().addOption(name, value);
            return this;
        }

        @Override
        default <V> V option(OptionName<V> name) {
            return options().option(name);
        }

        @Override
        default <V> V option(String name) {
            return options().option(name);
        }

        @Override
        default <V> V removeOption(OptionName<V> name) {
            return options().removeOption(name);
        }

        @Override
        default <V> V removeOption(String name) {
            return options().removeOption(name);
        }

        @Override
        default Map<String, Object> items() {
            return options().items();
        }
    }

    /**
     * Builds {@link Supplier} instance and defines options.
     */
    interface Builder<T, SELF extends Builder<T, SELF>> extends FluentBuilder<T, SELF>, Supplier {

        @Override
        default <V> SELF addOption(OptionName<V> name, V value) {
            options().addOption(name, value);
            return self();
        }

        @Override
        default <V> SELF addOption(String name, V value) {
            options().addOption(name, value);
            return self();
        }
    }

}



