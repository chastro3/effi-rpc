package io.effi.rpc.common.url;

import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;

import java.util.*;

/**
 * Represents a configuration that stores key-value pairs,
 * supporting different data types such as String, boolean,
 * int, and long. Configurations can inherit values from a
 * parent configuration.
 */
public class Config {

    private final Map<String, String> properties = new HashMap<>();

    private Config parent;

    public Config() {
        this(null);
    }

    /**
     * Creates a new configuration instance with an optional parent configuration.
     *
     * @param parent the parent configuration, or null if no parent exists
     */
    public Config(Config parent) {
        this.parent = parent;
    }

    /**
     * Sets the parent configuration.
     *
     * @param parent the parent configuration
     * @return the updated configuration instance
     */
    public Config parent(Config parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Sets a config value.
     *
     * @param key   the key
     * @param value the value (ignored if null)
     */
    public void set(String key, String value) {
        if (value != null) {
            properties.put(key, value);
        }
    }

    /**
     * Sets multiple config values at once.
     *
     * @param properties the properties to be added
     */
    public void set(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    /**
     * Retrieves a config value based on the given {@link ConfigKey}, considering its source strategy.
     *
     * @param configKey the config key containing the key name, source, and default value
     * @return the resolved value, or the default value if not found
     */
    public String get(ConfigKey configKey) {
        String key = configKey.key();
        Source source = configKey.source();
        String value = null;
        if (source == Source.NULL_OR_FROM_PARENT) {
            value = getOrFromParent(key);
        } else if (source == Source.ONLY_FROM_PARENT) {
            value = getOnlyFromParent(key);
        } else if (source == Source.MERGE_FROM_PARENT) {
            List<String> values = getMerged(key);
            if (CollectionUtil.isNotEmpty(values)) {
                value = String.join(",", values);
            }
        } else {
            value = get(key);
        }
        return StringUtil.isBlank(value) ? configKey.defaultValue() : value;
    }

    public List<String> getMerged(ConfigKey configKey) {
        return configKey.source() == Config.Source.MERGE_FROM_PARENT
                ? getMerged(configKey.key())
                : Collections.emptyList();
    }

    /**
     * Retrieves a config value.
     *
     * @param key the key
     * @return the value, or null if not found
     */
    public String get(String key) {
        return properties.get(key);
    }

    /**
     * Retrieves a config value, returning a default value if not found.
     *
     * @param key          the key
     * @param defaultValue the default value to return if the key is not found
     * @return the value, or the default value if not found
     */
    public String get(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * Retrieves the value associated with the given key.
     * If not found, attempts to fetch it from the parent configuration.
     *
     * @param key the key to look up
     * @return the value, or null if neither this nor the parent configuration contains it
     */
    public String getOrFromParent(String key) {
        String value = this.get(key);
        if (value == null && parent != null) {
            value = parent.getOrFromParent(key);
            if (value != null) {
                this.set(key, value);
            }
        }
        return value;
    }

    /**
     * Retrieves the configuration value only from the parent.
     *
     * @param key the key to look up
     * @return the value from the parent, or from the current config if not found in the parent
     */
    public String getOnlyFromParent(String key) {
        if (parent != null) {
            String value = parent.getOnlyFromParent(key);
            if (value != null) {
                return value;
            }
        }
        return this.get(key);
    }

    /**
     * Retrieves and merges configuration values from both the current and parent configurations.
     * The parent values are added first, followed by the current values.
     *
     * @param key the key to look up
     * @return a list of values, starting with parent values, then current config values
     */
    public List<String> getMerged(String key) {
        List<String> mergedList = new ArrayList<>();
        if (parent != null) {
            List<String> values = parent.getMerged(key);
            if (CollectionUtil.isNotEmpty(values)) {
                mergedList.addAll(values);
            }
        }
        String value = this.get(key);
        if (value != null) {
            mergedList.add(value);
        }
        return mergedList;
    }

    /**
     * Retrieves a boolean value from the configuration.
     *
     * @param key the key
     * @return the boolean value, or false if not found or invalid
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Retrieves a boolean value, returning a default if not found.
     *
     * @param key          the key
     * @param defaultValue the default value if the key is not found
     * @return the boolean value, or the default value if not found
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return StringUtil.isBlank(value) ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Retrieves an integer value from the configuration.
     *
     * @param key the key
     * @return the integer value
     * @throws NumberFormatException if the value is not a valid integer
     */
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Retrieves an integer value, returning a default if not found.
     *
     * @param key          the key
     * @param defaultValue the default value if the key is not found
     * @return the integer value, or the default value if not found
     */
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        return StringUtil.isBlank(value) ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Retrieves a long value from the configuration.
     *
     * @param key the key
     * @return the long value
     * @throws NumberFormatException if the value is not a valid long
     */
    public long getLong(String key) {
        return Long.parseLong(get(key));
    }

    /**
     * Retrieves a long value, returning a default if not found.
     *
     * @param key          the key
     * @param defaultValue the default value if the key is not found
     * @return the long value, or the default value if not found
     */
    public long getLong(String key, long defaultValue) {
        String value = get(key);
        return StringUtil.isBlank(value) ? defaultValue : Long.parseLong(value);
    }

    /**
     * Removes a config value by key.
     *
     * @param key the key to remove
     */
    public void remove(String key) {
        properties.remove(key);
    }

    /**
     * Returns an unmodifiable view of the configuration properties.
     */
    public Map<String, String> properties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public String toString() {
        return URLUtil.toQueryParam(properties);
    }

    /**
     * Represents the source of a configuration value.
     */
    public enum Source {

        /**
         * Uses value from the current configuration or falls back to the parent if not present.
         */
        NULL_OR_FROM_PARENT,

        /**
         * Retrieves value only from the parent configuration.
         */
        ONLY_FROM_PARENT,

        /**
         * Merges values from the parent and current configuration.
         */
        MERGE_FROM_PARENT,

        /**
         * Retrieves value only from the current configuration.
         */
        SELF
    }
}

