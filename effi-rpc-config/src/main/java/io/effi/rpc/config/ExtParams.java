package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;

import java.util.Map;

/**
 * Manages extension parameters.
 */
public interface ExtParams extends BaseParams, ConfigSource {

    @Override
    default ExtParams addParam(String key, String value) {
        config().set(key, value);
        return this;
    }

    @Override
    default ExtParams addParams(Map<String, String> params) {
        if (CollectionUtil.isNotEmpty(params)) {
            config().set(params);
        }
        return this;
    }

    @Override
    default String getParam(String key) {
        return config().get(key);
    }

    @Override
    default String getParam(String key, String defaultValue) {
        return config().getOrDefault(key, defaultValue);
    }

    @Override
    default ExtParams removeParam(String key) {
        config().remove(key);
        return this;
    }

    /**
     * Retrieves a parameter value by key.
     */
    default String getParam(ConfigKey configKey) {
        return config().get(configKey);
    }

    /**
     * Retrieves a boolean parameter value.
     */
    default boolean getBooleanParam(ConfigKey configKey) {
        return Boolean.parseBoolean(getParam(configKey));
    }

    /**
     * Retrieves an int parameter value.
     */
    default int getIntParam(ConfigKey configKey) {
        return Integer.parseInt(getParam(configKey));
    }

    /**
     * Retrieves a long parameter value.
     */
    default long getLongParam(ConfigKey configKey) {
        return Long.parseLong(getParam(configKey));
    }

}

