package io.effi.rpc.async;

public interface Result<T> {

    boolean succeeded();

    boolean failed();

    T result();

    Throwable cause();

    static <R> Result<R> success(R result) {
        return new SucceededResult<>(result);
    }

    static <R> Result<R> failure(Throwable cause) {
        return new FailedResult<>(cause);
    }
}