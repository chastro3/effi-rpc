package io.effi.rpc.option.support;

import io.effi.rpc.option.OptionType;

/**
 * Creates standard scalar option types.
 */
public final class ScalarTypeSupport {

    /**
     * Creates a string option type.
     */
    public static OptionType<String> string() {
        return new StandardOptionType<>(String::valueOf);
    }

    /**
     * Creates a byte option type.
     */
    public static OptionType<Byte> byteValue() {
        return new StandardOptionType<>(value -> {
            if (value instanceof Number number) {
                return number.byteValue();
            }
            return Byte.valueOf(String.valueOf(value));
        });
    }

    /**
     * Creates a short option type.
     */
    public static OptionType<Short> shortValue() {
        return new StandardOptionType<>(value -> {
            if (value instanceof Number number) {
                return number.shortValue();
            }
            return Short.valueOf(String.valueOf(value));
        });
    }

    /**
     * Creates an integer option type.
     */
    public static OptionType<Integer> integer() {
        return new StandardOptionType<>(value -> {
            if (value instanceof Number number) {
                return number.intValue();
            }
            return Integer.valueOf(String.valueOf(value));
        });
    }

    /**
     * Creates a long option type.
     */
    public static OptionType<Long> longValue() {
        return new StandardOptionType<>(value -> {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.valueOf(String.valueOf(value));
        });
    }

    /**
     * Creates a float option type.
     */
    public static OptionType<Float> floatValue() {
        return new StandardOptionType<>(value -> {
            if (value instanceof Number number) {
                return number.floatValue();
            }
            return Float.valueOf(String.valueOf(value));
        });
    }

    /**
     * Creates a double option type.
     */
    public static OptionType<Double> doubleValue() {
        return new StandardOptionType<>(value -> {
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            return Double.valueOf(String.valueOf(value));
        });
    }

    /**
     * Creates a boolean option type.
     */
    public static OptionType<Boolean> booleanValue() {
        return new StandardOptionType<>(value -> {
            if (value instanceof Boolean bool) {
                return bool;
            }
            return Boolean.valueOf(String.valueOf(value));
        });
    }

    /**
     * Creates a character option type.
     */
    public static OptionType<Character> character() {
        return new StandardOptionType<>(value -> {
            if (value instanceof Character character) {
                return character;
            }
            String text = String.valueOf(value);
            if (text.length() != 1) {
                throw new IllegalArgumentException("Expected a single character but got: " + text);
            }
            return text.charAt(0);
        });
    }

    private ScalarTypeSupport() {
    }
}
