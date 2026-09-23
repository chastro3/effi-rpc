package io.effi.rpc.option.support;

import io.effi.rpc.option.ArrayOptionType;
import io.effi.rpc.option.OptionType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Creates standard array option types.
 */
public final class ArrayTypeSupport {

    /**
     * Creates a string array option type.
     */
    public static ArrayOptionType<String[]> stringArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toStringArray,
                ArrayTypeSupport::mergeStringArray
        );
    }

    /**
     * Creates a byte array option type.
     */
    public static ArrayOptionType<byte[]> byteArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toByteArray,
                ArrayTypeSupport::mergeByteArray
        );
    }

    /**
     * Creates a boxed byte array option type.
     */
    public static ArrayOptionType<Byte[]> byteObjectArray() {
        return new StandardArrayOptionType<>(
                value -> toObjectArray(value, Byte.class, ScalarTypeSupport.byteValue()),
                ArrayTypeSupport::mergeObjectArray
        );
    }

    /**
     * Creates a short array option type.
     */
    public static ArrayOptionType<short[]> shortArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toShortArray,
                ArrayTypeSupport::mergeShortArray
        );
    }

    /**
     * Creates a boxed short array option type.
     */
    public static ArrayOptionType<Short[]> shortObjectArray() {
        return new StandardArrayOptionType<>(
                value -> toObjectArray(value, Short.class, ScalarTypeSupport.shortValue()),
                ArrayTypeSupport::mergeObjectArray
        );
    }

    /**
     * Creates an integer array option type.
     */
    public static ArrayOptionType<int[]> intArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toIntArray,
                ArrayTypeSupport::mergeIntArray
        );
    }

    /**
     * Creates a boxed integer array option type.
     */
    public static ArrayOptionType<Integer[]> integerArray() {
        return new StandardArrayOptionType<>(
                value -> toObjectArray(value, Integer.class, ScalarTypeSupport.integer()),
                ArrayTypeSupport::mergeObjectArray
        );
    }

    /**
     * Creates a long array option type.
     */
    public static ArrayOptionType<long[]> longArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toLongArray,
                ArrayTypeSupport::mergeLongArray
        );
    }

    /**
     * Creates a boxed long array option type.
     */
    public static ArrayOptionType<Long[]> longObjectArray() {
        return new StandardArrayOptionType<>(
                value -> toObjectArray(value, Long.class, ScalarTypeSupport.longValue()),
                ArrayTypeSupport::mergeObjectArray
        );
    }

    /**
     * Creates a float array option type.
     */
    public static ArrayOptionType<float[]> floatArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toFloatArray,
                ArrayTypeSupport::mergeFloatArray
        );
    }

    /**
     * Creates a boxed float array option type.
     */
    public static ArrayOptionType<Float[]> floatObjectArray() {
        return new StandardArrayOptionType<>(
                value -> toObjectArray(value, Float.class, ScalarTypeSupport.floatValue()),
                ArrayTypeSupport::mergeObjectArray
        );
    }

    /**
     * Creates a double array option type.
     */
    public static ArrayOptionType<double[]> doubleArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toDoubleArray,
                ArrayTypeSupport::mergeDoubleArray
        );
    }

    /**
     * Creates a boxed double array option type.
     */
    public static ArrayOptionType<Double[]> doubleObjectArray() {
        return new StandardArrayOptionType<>(
                value -> toObjectArray(value, Double.class, ScalarTypeSupport.doubleValue()),
                ArrayTypeSupport::mergeObjectArray
        );
    }

    /**
     * Creates a boolean array option type.
     */
    public static ArrayOptionType<boolean[]> booleanArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toBooleanArray,
                ArrayTypeSupport::mergeBooleanArray
        );
    }

    /**
     * Creates a boxed boolean array option type.
     */
    public static ArrayOptionType<Boolean[]> booleanObjectArray() {
        return new StandardArrayOptionType<>(
                value -> toObjectArray(value, Boolean.class, ScalarTypeSupport.booleanValue()),
                ArrayTypeSupport::mergeObjectArray
        );
    }

    /**
     * Creates a character array option type.
     */
    public static ArrayOptionType<char[]> charArray() {
        return new StandardArrayOptionType<>(
                ArrayTypeSupport::toCharArray,
                ArrayTypeSupport::mergeCharArray
        );
    }

    /**
     * Creates a boxed character array option type.
     */
    public static ArrayOptionType<Character[]> characterArray() {
        return new StandardArrayOptionType<>(
                value -> toObjectArray(value, Character.class, ScalarTypeSupport.character()),
                ArrayTypeSupport::mergeObjectArray
        );
    }

    private static String[] toStringArray(Object value) {
        if (value instanceof String[] array) {
            return array.clone();
        }
        return toList(value).stream()
                .map(String::valueOf)
                .toArray(String[]::new);
    }

    private static byte[] toByteArray(Object value) {
        if (value instanceof byte[] array) {
            return array.clone();
        }
        List<?> values = toList(value);
        byte[] result = new byte[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ScalarTypeSupport.byteValue().convert(values.get(i));
        }
        return result;
    }

    private static short[] toShortArray(Object value) {
        if (value instanceof short[] array) {
            return array.clone();
        }
        List<?> values = toList(value);
        short[] result = new short[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ScalarTypeSupport.shortValue().convert(values.get(i));
        }
        return result;
    }

    private static int[] toIntArray(Object value) {
        if (value instanceof int[] array) {
            return array.clone();
        }
        List<?> values = toList(value);
        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ScalarTypeSupport.integer().convert(values.get(i));
        }
        return result;
    }

    private static long[] toLongArray(Object value) {
        if (value instanceof long[] array) {
            return array.clone();
        }
        List<?> values = toList(value);
        long[] result = new long[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ScalarTypeSupport.longValue().convert(values.get(i));
        }
        return result;
    }

    private static float[] toFloatArray(Object value) {
        if (value instanceof float[] array) {
            return array.clone();
        }
        List<?> values = toList(value);
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ScalarTypeSupport.floatValue().convert(values.get(i));
        }
        return result;
    }

    private static double[] toDoubleArray(Object value) {
        if (value instanceof double[] array) {
            return array.clone();
        }
        List<?> values = toList(value);
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ScalarTypeSupport.doubleValue().convert(values.get(i));
        }
        return result;
    }

    private static boolean[] toBooleanArray(Object value) {
        if (value instanceof boolean[] array) {
            return array.clone();
        }
        List<?> values = toList(value);
        boolean[] result = new boolean[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ScalarTypeSupport.booleanValue().convert(values.get(i));
        }
        return result;
    }

    private static char[] toCharArray(Object value) {
        if (value instanceof char[] array) {
            return array.clone();
        }
        if (value instanceof CharSequence text) {
            return text.toString().toCharArray();
        }
        return String.valueOf(value).toCharArray();
    }

    private static <T> T[] toObjectArray(
            Object value,
            Class<T> componentType,
            OptionType<T> scalarType
    ) {
        if (value.getClass().isArray()
                && componentType.isAssignableFrom(value.getClass().getComponentType())) {
            @SuppressWarnings("unchecked")
            T[] array = (T[]) value;
            return array.clone();
        }
        List<?> values = toList(value);
        @SuppressWarnings("unchecked")
        T[] result = (T[]) java.lang.reflect.Array.newInstance(componentType, values.size());
        for (int i = 0; i < values.size(); i++) {
            result[i] = scalarType.convert(values.get(i));
        }
        return result;
    }

    private static <T> T[] mergeObjectArray(T[] parent, T[] current) {
        LinkedHashSet<T> result = new LinkedHashSet<>();
        Collections.addAll(result, parent);
        Collections.addAll(result, current);
        @SuppressWarnings("unchecked")
        T[] merged = (T[]) java.lang.reflect.Array.newInstance(
                parent.getClass().getComponentType(),
                result.size()
        );
        return result.toArray(merged);
    }

    private static String[] mergeStringArray(String[] parent, String[] current) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Collections.addAll(result, parent);
        Collections.addAll(result, current);
        return result.toArray(String[]::new);
    }

    private static byte[] mergeByteArray(byte[] parent, byte[] current) {
        byte[] result = new byte[parent.length + current.length];
        System.arraycopy(parent, 0, result, 0, parent.length);
        System.arraycopy(current, 0, result, parent.length, current.length);
        return result;
    }

    private static short[] mergeShortArray(short[] parent, short[] current) {
        short[] result = new short[parent.length + current.length];
        System.arraycopy(parent, 0, result, 0, parent.length);
        System.arraycopy(current, 0, result, parent.length, current.length);
        return result;
    }

    private static int[] mergeIntArray(int[] parent, int[] current) {
        int[] result = new int[parent.length + current.length];
        System.arraycopy(parent, 0, result, 0, parent.length);
        System.arraycopy(current, 0, result, parent.length, current.length);
        return result;
    }

    private static long[] mergeLongArray(long[] parent, long[] current) {
        long[] result = new long[parent.length + current.length];
        System.arraycopy(parent, 0, result, 0, parent.length);
        System.arraycopy(current, 0, result, parent.length, current.length);
        return result;
    }

    private static float[] mergeFloatArray(float[] parent, float[] current) {
        float[] result = new float[parent.length + current.length];
        System.arraycopy(parent, 0, result, 0, parent.length);
        System.arraycopy(current, 0, result, parent.length, current.length);
        return result;
    }

    private static double[] mergeDoubleArray(double[] parent, double[] current) {
        double[] result = new double[parent.length + current.length];
        System.arraycopy(parent, 0, result, 0, parent.length);
        System.arraycopy(current, 0, result, parent.length, current.length);
        return result;
    }

    private static boolean[] mergeBooleanArray(boolean[] parent, boolean[] current) {
        boolean[] result = new boolean[parent.length + current.length];
        System.arraycopy(parent, 0, result, 0, parent.length);
        System.arraycopy(current, 0, result, parent.length, current.length);
        return result;
    }

    private static char[] mergeCharArray(char[] parent, char[] current) {
        char[] result = new char[parent.length + current.length];
        System.arraycopy(parent, 0, result, 0, parent.length);
        System.arraycopy(current, 0, result, parent.length, current.length);
        return result;
    }

    private static List<?> toList(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        if (value instanceof Collection<?> collection) {
            return List.copyOf(collection);
        }
        if (value instanceof Object[] array) {
            return List.of(array);
        }
        return List.of(String.valueOf(value).split(","));
    }

    private ArrayTypeSupport() {
    }
}
