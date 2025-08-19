package io.effi.rpc.async;

import io.effi.rpc.util.AssertUtil;

public record FailedResult<T>(Throwable cause) implements Result<T> {

    public FailedResult(Throwable cause) {
        this.cause = AssertUtil.notNull(cause, "cause");
    }

    @Override
    public boolean succeeded() {
        return false;
    }

    @Override
    public boolean failed() {
        return true;
    }

    @Override
    public T result() {
        return null;
    }
}