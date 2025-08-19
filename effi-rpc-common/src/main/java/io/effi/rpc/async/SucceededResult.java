package io.effi.rpc.async;

public record SucceededResult<T>(T result) implements Result<T> {

    public SucceededResult(T result) {
        this.result = checkResult(result);
    }

    @Override
    public boolean succeeded() {
        return true;
    }

    @Override
    public boolean failed() {
        return false;
    }

    @Override
    public Throwable cause() {
        return null;
    }

    private T checkResult(T result) {
        if (result instanceof Throwable) {
            throw new IllegalArgumentException("Result can not be a throwable");
        }
        return result;
    }
}