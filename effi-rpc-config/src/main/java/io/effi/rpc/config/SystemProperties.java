package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;

import java.util.Map;

/**
 * Provides access to system properties.
 */
public final class SystemProperties implements BaseParams {

    private static final SystemProperties INSTANCE = new SystemProperties();

    private SystemProperties() {
    }

    public static SystemProperties getInstance() {
        return INSTANCE;
    }

    @Override
    public BaseParams addParam(String key, String value) {
        System.setProperty(key, value);
        return this;
    }

    @Override
    public BaseParams addParams(Map<String, String> params) {
        if (CollectionUtil.isNotEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public String getParam(String key) {
        return System.getProperty(key);
    }

    @Override
    public String getParam(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    @Override
    public BaseParams removeParam(String key) {
        System.clearProperty(key);
        return this;
    }
}
