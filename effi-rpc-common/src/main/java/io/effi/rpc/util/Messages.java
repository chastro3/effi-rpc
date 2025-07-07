package io.effi.rpc.util;

/**
 * Provides common messages.
 */
public class Messages {

    /**
     * Generates a message indicating only the supported message type is allowed.
     */
    public static String onlySupport(Class<?> type) {
        if (type != null) {
            return "Only supports messages of type " + type.getName();
        }
        return "Invalid class type";
    }

    /**
     * Generates a message indicating the specified type is not supported.
     */
    public static String unSupport(String name, Class<?> type) {
        if (type != null) {
            name = StringUtil.isBlank(name) ? "" : name;
            return "Unsupported '" + name + "' type " + type.getName();
        }
        return "Invalid class type";
    }

    /**
     * Generates a message indicating the specified path cannot be parsed.
     */
    public static String parseFile(String path) {
        return "Failed to parse '" + path + "'";
    }

    /**
     * Generates a message indicating the specified name cannot be null.
     */
    public static String notNull(String name) {
        if (name != null) {
            return name + " cannot be null";
        }
        return "Invalid name";
    }

    /**
     * Generates a message indicating the specified name cannot be blank.
     */
    public static String notBlank(String name) {
        if (StringUtil.isNotBlank(name)) {
            return name + " cannot be blank";
        }
        return "Invalid name";
    }
}


