package io.effi.rpc.config;

import io.effi.rpc.util.StringUtil;

import java.util.Map;

/**
 * Provides methods for retrieving parameters.
 */
public interface BaseParams {

    /**
     * Adds a single parameter.
     */
    BaseParams addParam(String key, String value);

    /**
     * Adds multiple parameters.
     */
    BaseParams addParams(Map<String, String> params);

    /**
     * Retrieves a parameter value by key.
     */
    String getParam(String key);

    /**
     * Retrieves a parameter value or the default if absent.
     */
    String getParam(String key, String defaultValue);

    /**
     * Removes a parameter by key.
     */
    BaseParams removeParam(String key);

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
}
