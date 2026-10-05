package io.effi.rpc.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorCodeTest {

    @Test
    void predefinedCodesAreUnique() {
        Set<String> codes = Arrays.stream(PredefinedErrorCode.values())
                .map(PredefinedErrorCode::code)
                .collect(Collectors.toSet());

        assertEquals(PredefinedErrorCode.values().length, codes.size());
    }

    @Test
    void defaultErrorCodesUseValueEquality() {
        DefaultErrorCode first = DefaultErrorCode.valueOf("test_001", "failure");
        DefaultErrorCode second = DefaultErrorCode.valueOf("test_001", "failure");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
