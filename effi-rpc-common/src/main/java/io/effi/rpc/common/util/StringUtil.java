package io.effi.rpc.common.util;

import java.util.Objects;

/**
 * Utility class for string operations.
 */
public final class StringUtil {

    private StringUtil() {
    }

    /**
     * Checks if a CharSequence is null, empty, or contains only whitespace characters.
     *
     * @param str the CharSequence to check
     * @return true if the CharSequence is null, empty, or only contains whitespace
     */
    public static boolean isBlank(CharSequence str) {
        return (str == null || str.isEmpty() || isWhitespace(str));
    }

    /**
     * Checks if a CharSequence is not blank (i.e., it is not null, not empty,
     * and contains non-whitespace characters).
     *
     * @param str the CharSequence to check
     * @return true if the CharSequence is not blank
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * Returns the target string if it is not blank; otherwise, returns the specified default value.
     *
     * @param target       the string to check
     * @param defaultValue the default value to return if the target is blank
     * @return the target string if it is not blank, otherwise the default value
     */
    public static String isBlankOrDefault(String target, String defaultValue) {
        return isBlank(target) ? defaultValue : target;
    }

    /**
     * Compares two CharSequences for equality.
     *
     * @param c1 the first CharSequence to compare
     * @param c2 the second CharSequence to compare
     * @return true if the two CharSequences are equal, false otherwise
     */
    public static boolean equals(CharSequence c1, CharSequence c2) {
        return CharSequence.compare(c1, c2) == 0;
    }

    /**
     * Formats a message by replacing placeholders ({}) with provided arguments.
     *
     * @param message the format string containing placeholders
     * @param args    the arguments to replace the placeholders
     * @return the formatted string with placeholders replaced by the corresponding arguments
     */
    public static String format(String message, Object... args) {
        if (StringUtil.isBlank(message) || args == null || args.length == 0) {
            return message;
        }
        StringBuilder result = new StringBuilder(message.length() + args.length * 10);
        int argIndex = 0;
        for (int i = 0; i < message.length(); i++) {
            if (argIndex >= args.length) {
                // If all parameters have been used, append the remaining strings
                result.append(message, i, message.length());
                break;
            }
            char currentChar = message.charAt(i);
            if (currentChar == '{' && i + 1 < message.length() && message.charAt(i + 1) == '}') {
                result.append(Objects.toString(args[argIndex], "null"));
                argIndex++;
                i++; // skip '}'
            } else {
                result.append(currentChar);
            }
        }
        return result.toString();
    }

    /**
     * Checks if a CharSequence contains only whitespace characters.
     *
     * @param str the CharSequence to check
     * @return true if the CharSequence contains only whitespace, false otherwise
     */
    private static boolean isWhitespace(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

