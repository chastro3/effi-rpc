package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;

/**
 * Immutable terminal result of an asynchronous RPC operation.
 */
public interface Result<T> {

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(EffiRpcException cause) {
        return new Failure<>(cause);
    }

    boolean succeeded();

    default boolean failed() {
        return !succeeded();
    }

    T value();

    EffiRpcException cause();

    default T requireValue() {
        if (failed()) {
            throw cause();
        }
        return value();
    }

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
