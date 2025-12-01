package io.effi.rpc.concurrent;

/**
 * Represents the outcome of an operation producing either a result or a failure.
 * <p>
 * Provides access to success and failure states, including the produced value
 * or the underlying cause when an operation does not complete successfully.
 */
public interface Result<T> {

    /**
     * Returns true if the operation completed successfully.
     */
    boolean succeeded();

    /**
     * Returns true if the operation failed.
     */
    boolean failed();

    /**
     * Returns the result value if the operation succeeded.
     */
    T result();

    /**
     * Returns the cause of failure if the operation did not succeed.
     */
    Throwable cause();

    /**
     * Creates a successful result containing the given value.
     */
    static <R> Result<R> success(R result) {
        return new SucceededResult<>(result);
    }

    /**
     * Creates a failed result containing the given cause.
     */
    static <R> Result<R> failure(Throwable cause) {
        return new FailedResult<>(cause);
    }
}
