package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Map;

/**
 * Manages extension parameters.
 */
public interface ExtParams extends ConfigSource {

    /**
     * Adds a single parameter.
     */
    default ExtParams addParam(String key, String value) {
        config().set(key, value);
        return this;
    }

    /**
     * Adds multiple parameters.
     */
    default ExtParams addParams(Map<String, String> params) {
        if (CollectionUtil.isNotEmpty(params)) {
            config().set(params);
        }
        return this;
    }

    /**
     * Retrieves a parameter value by key.
     */
    default String getParam(ConfigKey configKey) {
        return config().get(configKey);
    }

    /**
     * Retrieves a parameter value by key.
     */
    default String getParam(String key) {
        return config().get(key);
    }

    /**
     * Retrieves a parameter value or the default if absent.
     */
    default String getParam(String key, String defaultValue) {
        return config().getOrDefault(key, defaultValue);
    }

    /**
     * Retrieves a boolean parameter value.
     */
    default boolean getBooleanParam(ConfigKey configKey) {
        return Boolean.parseBoolean(getParam(configKey));
    }

    /**
     * Retrieves a boolean parameter value.
     */
    default boolean getBooleanParam(String key) {
        return Boolean.parseBoolean(getParam(key));
    }

    /**
     * Retrieves a boolean value or the default if absent or blank.
     */
    default boolean getBooleanParam(String key, boolean defaultValue) {
        String value = getParam(key);
        return StringUtil.isBlank(value) ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Retrieves an int parameter value.
     */
    default int getIntParam(ConfigKey configKey) {
        return Integer.parseInt(getParam(configKey));
    }

    /**
     * Retrieves an int parameter value.
     */
    default int getIntParam(String key) {
        return Integer.parseInt(getParam(key));
    }

    /**
     * Retrieves an int value or the default if absent or blank.
     */
    default int getIntParam(String key, int defaultValue) {
        String value = getParam(key);
        return StringUtil.isBlank(value) ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Retrieves a long parameter value.
     */
    default long getLongParam(ConfigKey configKey) {
        return Long.parseLong(getParam(configKey));
    }

    /**
     * Retrieves a long parameter value.
     */
    default long getLongParam(String key) {
        return Long.parseLong(getParam(key));
    }

    /**
     * Retrieves a long value or the default if absent or blank.
     */
    default long getLongParam(String key, long defaultValue) {
        String value = getParam(key);
        return StringUtil.isBlank(value) ? defaultValue : Long.parseLong(value);
    }

    /**
     * Removes a parameter by key.
     */
    default ExtParams removeParam(String key) {
        config().remove(key);
        return this;
    }
}

