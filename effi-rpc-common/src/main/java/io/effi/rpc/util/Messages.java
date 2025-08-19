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
}


