package io.effi.rpc.config;

import io.effi.rpc.util.FluentBuilder;

/**
 * Builds {@link Config.Supplier} instance and defines configuration.
 */
public interface ConfigBuilder<T, SELF extends ConfigBuilder<T, SELF>> extends Config.Supplier, FluentBuilder<T, SELF> {

    default <V> SELF setConfig(ConfigName<V> name, V value) {
        config().set(name.name(), value);
        return self();
    }

    default <V> SELF setConfig(String name, V value) {
        config().set(name, value);
        return self();
    }

    default <V> SELF removeConfig(ConfigName<V> name) {
        config().remove(name.name());
        return self();
    }

    default SELF removeConfig(String name) {
        config().remove(name);
        return self();
    }

}
