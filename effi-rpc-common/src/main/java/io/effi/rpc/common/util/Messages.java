package io.effi.rpc.common.util;

/**
 * Utility class for generating common messages.
 */
public class Messages {

    /**
     * Generates a message indicating that only the supported message type is allowed.
     *
     * @param type the class type to be checked
     * @return a message describing the supported type or an error message if the type is null
     */
    public static String onlySupport(Class<?> type) {
        if (type != null) {
            return "Only supports messages of type " + type.getName();
        }
        return "Invalid class type";
    }

    /**
     * Generates a message indicating that the specified type is not supported.
     *
     * @param name the name of the entity being checked
     * @param type the class type to be checked
     * @return a message describing the unsupported type or an error message if the type is null
     */
    public static String unSupport(String name, Class<?> type) {
        if (type != null) {
            // Ensure 'name' is not null or blank
            name = StringUtil.isBlank(name) ? "" : name;
            return "Unsupported " + name + " type " + type.getName();
        }
        return "Invalid class type";
    }

    /**
     * Generates a message indicating that the specified name cannot be null.
     *
     * @param name the name to be checked
     * @return a message stating that the name cannot be null, or an error message if the name is null
     */
    public static String notNull(String name) {
        if (name != null) {
            return name + " cannot be null";
        }
        return "Invalid name";
    }

    /**
     * Generates a message indicating that the specified name cannot be blank.
     *
     * @param name the name to be checked
     * @return a message stating that the name cannot be blank, or an error message if the name is blank
     */
    public static String notBlank(String name) {
        if (StringUtil.isNotBlank(name)) {
            return name + " cannot be blank";
        }
        return "Invalid name";
    }
}

