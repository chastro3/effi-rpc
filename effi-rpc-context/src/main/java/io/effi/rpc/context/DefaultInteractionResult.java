package io.effi.rpc.context;

import io.effi.rpc.concurrent.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;

final class DefaultInteractionResult<T> implements Interaction.Result {

    private final SmartURL url;

    private final Result<T> result;

    private DefaultInteractionResult(SmartURL url, Result<T> result) {
        this.url = AssertUtil.notNull(url, "url");
        this.result = result;
    }

    static <T> DefaultInteractionResult<T> success(SmartURL url, T result) {
        return new DefaultInteractionResult<>(url, Result.success(result));
    }

    static <T> DefaultInteractionResult<T> failure(SmartURL url, EffiRpcException cause) {
        return new DefaultInteractionResult<>(url, Result.failure(cause));
    }

    @Override
    public SmartURL url() {
        return url;
    }

    @Override
    public EffiRpcException cause() {
        return result.cause();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R excepted() {
        return (R) result.requireValue();
    }

    @Override
    public boolean failed() {
        return result.failed();
    }

    @Override
    public T value() {
        return result.value();
    }

    @Override
    public boolean succeeded() {
        return result.succeeded();
    }
}
