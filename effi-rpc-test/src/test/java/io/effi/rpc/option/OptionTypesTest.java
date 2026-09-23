package io.effi.rpc.option;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptionTypesTest {

    @Test
    void convertsScalarValues() {
        assertEquals("123", OptionTypes.STRING.convert(123));
        assertEquals(12, OptionTypes.INTEGER.convert("12"));
        assertEquals(12L, OptionTypes.LONG.convert(12));
        assertEquals(1.5D, OptionTypes.DOUBLE.convert("1.5"));
        assertEquals(true, OptionTypes.BOOLEAN.convert("true"));
        assertEquals('a', OptionTypes.CHARACTER.convert("a"));
    }

    @Test
    void rejectsInvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> OptionTypes.CHARACTER.convert("abc"));
    }

    @Test
    void convertsStringArrayFromCommaSeparatedValue() {
        assertArrayEquals(
                new String[]{"a", "b", "c"},
                OptionTypes.STRING_ARRAY.convert("a,b,c")
        );
    }

    @Test
    void convertsPrimitiveArrays() {
        assertArrayEquals(
                new int[]{1, 2, 3},
                OptionTypes.INT_ARRAY.convert("1,2,3")
        );
        assertArrayEquals(
                new long[]{4L, 5L, 6L},
                OptionTypes.LONG_ARRAY.convert("4,5,6")
        );
    }

    @Test
    void convertsAndMergesBoxedArrays() {
        assertArrayEquals(
                new Integer[]{1, 2, 3},
                OptionTypes.INTEGER_ARRAY.convert("1,2,3")
        );
        assertArrayEquals(
                new Integer[]{1, 2, 3, 4},
                OptionTypes.INTEGER_ARRAY.merge(
                        new Integer[]{1, 2},
                        new Integer[]{3, 4}
                )
        );
    }

    @Test
    void mergesStringArrayWithDistinctOrder() {
        assertArrayEquals(
                new String[]{"a", "b", "c"},
                OptionTypes.STRING_ARRAY.merge(
                        new String[]{"a", "b"},
                        new String[]{"b", "c"}
                )
        );
    }

    @Test
    void mergesPrimitiveArrays() {
        assertArrayEquals(
                new int[]{1, 2, 3, 4},
                OptionTypes.INT_ARRAY.merge(
                        new int[]{1, 2},
                        new int[]{3, 4}
                )
        );
        assertArrayEquals(
                new long[]{1L, 2L, 3L},
                OptionTypes.LONG_ARRAY.merge(
                        new long[]{1L},
                        new long[]{2L, 3L}
                )
        );
    }
}
