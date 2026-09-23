package io.effi.rpc.option;

import io.effi.rpc.option.support.ArrayTypeSupport;
import io.effi.rpc.option.support.ScalarTypeSupport;

/**
 * Provides built-in option types.
 */
public final class OptionTypes {

    public static final OptionType<String> STRING = ScalarTypeSupport.string();

    public static final OptionType<Byte> BYTE = ScalarTypeSupport.byteValue();

    public static final OptionType<Short> SHORT = ScalarTypeSupport.shortValue();

    public static final OptionType<Integer> INTEGER = ScalarTypeSupport.integer();

    public static final OptionType<Long> LONG = ScalarTypeSupport.longValue();

    public static final OptionType<Float> FLOAT = ScalarTypeSupport.floatValue();

    public static final OptionType<Double> DOUBLE = ScalarTypeSupport.doubleValue();

    public static final OptionType<Boolean> BOOLEAN = ScalarTypeSupport.booleanValue();

    public static final OptionType<Character> CHARACTER = ScalarTypeSupport.character();

    public static final ArrayOptionType<String[]> STRING_ARRAY = ArrayTypeSupport.stringArray();

    public static final ArrayOptionType<byte[]> BYTE_ARRAY = ArrayTypeSupport.byteArray();

    public static final ArrayOptionType<Byte[]> BYTE_OBJECT_ARRAY = ArrayTypeSupport.byteObjectArray();

    public static final ArrayOptionType<short[]> SHORT_ARRAY = ArrayTypeSupport.shortArray();

    public static final ArrayOptionType<Short[]> SHORT_OBJECT_ARRAY = ArrayTypeSupport.shortObjectArray();

    public static final ArrayOptionType<int[]> INT_ARRAY = ArrayTypeSupport.intArray();

    public static final ArrayOptionType<Integer[]> INTEGER_ARRAY = ArrayTypeSupport.integerArray();

    public static final ArrayOptionType<long[]> LONG_ARRAY = ArrayTypeSupport.longArray();

    public static final ArrayOptionType<Long[]> LONG_OBJECT_ARRAY = ArrayTypeSupport.longObjectArray();

    public static final ArrayOptionType<float[]> FLOAT_ARRAY = ArrayTypeSupport.floatArray();

    public static final ArrayOptionType<Float[]> FLOAT_OBJECT_ARRAY = ArrayTypeSupport.floatObjectArray();

    public static final ArrayOptionType<double[]> DOUBLE_ARRAY = ArrayTypeSupport.doubleArray();

    public static final ArrayOptionType<Double[]> DOUBLE_OBJECT_ARRAY = ArrayTypeSupport.doubleObjectArray();

    public static final ArrayOptionType<boolean[]> BOOLEAN_ARRAY = ArrayTypeSupport.booleanArray();

    public static final ArrayOptionType<Boolean[]> BOOLEAN_OBJECT_ARRAY = ArrayTypeSupport.booleanObjectArray();

    public static final ArrayOptionType<char[]> CHAR_ARRAY = ArrayTypeSupport.charArray();

    public static final ArrayOptionType<Character[]> CHARACTER_ARRAY = ArrayTypeSupport.characterArray();

    private OptionTypes() {
    }
}
