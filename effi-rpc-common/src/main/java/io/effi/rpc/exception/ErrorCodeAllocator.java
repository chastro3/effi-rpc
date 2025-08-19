package io.effi.rpc.exception;

import io.effi.rpc.util.AssertUtil;

/**
 * Allocates error codes with prefix and zero-padded numbers.
 * <p>
 * Generates sequentially numbered error codes with a specified prefix and
 * fixed-length numeric suffix, intended for static initialization use.
 * <p><b>Note:</b> Not thread-safe for concurrent use at runtime without external synchronization.
 */
public final class ErrorCodeAllocator {

    private final String prefix;

    private final int codeLength;

    private int counter;

    public ErrorCodeAllocator(String prefix) {
        this(prefix, 1, 3);
    }

    /**
     * Creates an {@link ErrorCodeAllocator} with the specified prefix, starting number, and code length.
     *
     * @param prefix    the prefix for error codes, must not be blank
     * @param startFrom the starting number for code generation
     * @param codeLength the length of the numeric part of the code
     * @throws IllegalArgumentException if codeLength is <= 0
     */
    public ErrorCodeAllocator(String prefix, int startFrom, int codeLength) {
        AssertUtil.notBlank(prefix, "prefix");
        if (codeLength <= 0) throw new IllegalArgumentException("Code length must be > 0");
        this.prefix = prefix.endsWith("_") ? prefix : prefix + "_";
        this.codeLength = codeLength;
        this.counter = startFrom;
    }

    /**
     * Generates the next error code with the specified message.
     *
     * @param message the error message associated with the code
     * @return a new ErrorCode instance
     */
    public ErrorCode next(String message) {
        return DefaultErrorCode.valueOf(nextCode(), message);
    }

    private String nextCode() {
        int num = counter++;
        char[] digits = new char[codeLength];
        for (int i = codeLength - 1; i >= 0; i--) {
            digits[i] = (char) ('0' + (num % 10));
            num /= 10;
        }
        return prefix + new String(digits);
    }
}

