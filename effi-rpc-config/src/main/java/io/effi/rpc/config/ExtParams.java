package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;

import java.util.Map;

/**
 * Manages extension parameters.
 */
public interface ExtParams extends BaseParams, Config.Provider {

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
     * Retrieves a boolean parameter value.
     */
    default boolean getBooleanParam(ConfigName configName) {
        return Boolean.parseBoolean(getParam(configName));
    }

    /**
     * Retrieves a parameter value by key.
     */
    default String getParam(ConfigName configName) {
        return config().get(configName);
    }

    /**
     * Retrieves an int parameter value.
     */
    default int getIntParam(ConfigName configName) {
        return Integer.parseInt(getParam(configName));
    }

    /**
     * Retrieves a long parameter value.
     */
    default long getLongParam(ConfigName configName) {
        return Long.parseLong(getParam(configName));
    }

}

