package io.effi.rpc.util;

/**
 * Provides number operations.
 */
public final class NumberUtil {

    /**
     * Converts an int[] to an Integer[].
     */
    public static Integer[] box(int[] array) {
        if (array == null) {
            return null;
        }
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Converts a long[] to a Long[].
     */
    public static Long[] box(long[] array) {
        if (array == null) {
            return null;
        }
        Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Converts a double[] to a Double[].
     */
    public static Double[] box(double[] array) {
        if (array == null) {
            return null;
        }
        Double[] result = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    private NumberUtil() {
    }

}
