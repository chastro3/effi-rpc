package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;

/**
 * Defines an immutable terminal result of an asynchronous RPC operation.
 * <p>
 * Failure results require a non-null cause.
 */
public interface Result<T> {

    /**
     * Creates a successful result.
     *
     * @param value success value
     * @return successful result
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed result.
     *
     * @param cause failure cause
     * @return failed result
     */
    static <T> Result<T> failure(EffiRpcException cause) {
        return new Failure<>(cause);
    }

    /**
     * Indicates whether this result is successful.
     */
    boolean succeeded();

    /**
     * Indicates whether this result represents a failure.
     */
    default boolean failed() {
        return !succeeded();
    }

    /**
     * Returns the success value, or {@code null} when failed.
     */
    T value();

    /**
     * Returns the failure cause, or {@code null} when successful.
     */
    EffiRpcException cause();

    /**
     * Returns the success value or throws the failure cause.
     *
     * @throws EffiRpcException if this result represents a failure
     */
    default T requireValue() {
        if (failed()) {
            throw cause();
        }
        return value();
    }

    /**
     * Stores a successful result.
     */
    record Success<T>(T value) implements Result<T> {

        @Override
        public boolean succeeded() {
            return true;
        }

        @Override
        public EffiRpcException cause() {
            return null;
        }
    }

    /**
     * Stores a failed result.
     */
    record Failure<T>(EffiRpcException cause) implements Result<T> {

        public Failure {
            AssertUtil.notNull(cause, "cause");
        }

        @Override
        public boolean succeeded() {
            return false;
        }

        @Override
        public T value() {
            return null;
        }
    }
}
