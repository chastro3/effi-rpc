package io.effi.rpc.context.support;

import io.effi.rpc.async.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;

public class DefaultInteractionResult<T> implements Interaction.Result {

    private final SmartURL url;

    private final Result<T> result;

    private DefaultInteractionResult(SmartURL url, Result<T> result) {
        this.url = AssertUtil.notNull(url, "url");
        this.result = result;
    }

    public static <T> DefaultInteractionResult<T> success(SmartURL url, T result) {
        return new DefaultInteractionResult<>(url, Result.success(result));
    }

    public static <T> DefaultInteractionResult<T> failure(SmartURL url, EffiRpcException cause) {
        return new DefaultInteractionResult<>(url, Result.failure(cause));
    }

    @Override
    public SmartURL url() {
        return url;
    }

    @Override
    public boolean succeeded() {
        return result.succeeded();
    }

    @Override
    public boolean failed() {
        return result.failed();
    }

    @Override
    public T result() {
        return result.result();
    }

    @Override
    public EffiRpcException cause() {
        return (EffiRpcException) result.cause();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R excepted() {
        return (R) result();
    }
}
