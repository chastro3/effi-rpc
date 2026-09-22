package io.effi.rpc.util;

/**
 * Provides exception utility operations.
 */
public final class ExceptionUtil {

    /**
     * Returns a non-blank message for the given exception.
     */
    public static String message(Throwable cause) {
        if (cause == null) return StringUtil.empty();
        String message = cause.getMessage();
        return StringUtil.isBlank(message)
                ? cause.getClass().getName()
                : message;
    }

    private ExceptionUtil() {
    }
}
