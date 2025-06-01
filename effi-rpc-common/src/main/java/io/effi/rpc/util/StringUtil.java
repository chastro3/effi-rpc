package io.effi.rpc.util;

import java.util.Objects;

/**
 * Provides string operations.
 */
public final class StringUtil {

    private static final String EMPTY = "";

    private static final String[]  EMPTY_ARRAY = new String[0];

    public static String empty(){
        return EMPTY;
    }

     public static String[] emptyArray(){
        return EMPTY_ARRAY;
    }

    /**
     * Checks if a CharSequence is null, empty, or contains only whitespace characters.
     */
    public static boolean isBlank(CharSequence str) {
        return (str == null || str.isEmpty() || isWhitespace(str));
    }

    /**
     * Checks if a CharSequence is not blank (i.e., it is not null, not empty,
     * and contains non-whitespace characters).
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * Returns the target string if it is not blank; otherwise, returns the specified default value.
     */
    public static String isBlankOrDefault(String target, String defaultValue) {
        return isBlank(target) ? defaultValue : target;
    }

    /**
     * Compares two CharSequences for equality.
     */
    public static boolean equals(CharSequence c1, CharSequence c2) {
        return CharSequence.compare(c1, c2) == 0;
    }

    /**
     * Formats a message by replacing placeholders ({}) with provided arguments.
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

    private StringUtil() {
    }
}

