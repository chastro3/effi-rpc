package io.effi.rpc.contract;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.util.GenericKey;
import io.effi.rpc.common.config.URL;

/**
 * Represent the result of an invocation,
 * which can either contain a value or an exception.
 */
public class Result {

    public static final GenericKey<Result> ATTRIBUTE_KEY = GenericKey.valueOf("result");

    private URL url;

    private Object value;

    private EffiRpcException exception;

    /**
     * Constructs a result with a URL and either a value or an exception.
     *
     * @param url   The URL associated with the result.
     * @param value The result value, or an exception if the invocation failed.
     */
    public Result(URL url, Object value) {
        this.url = url;
        if (value instanceof EffiRpcException e) {
            this.exception = e;
        } else {
            this.value = value;
        }
    }

    /**
     * Returns the config.
     */
    public URL url() {
        return url;
    }

    /**
     * Sets the config.
     *
     * @param url config
     */
    public Result url(URL url) {
        this.url = url;
        return this;
    }

    /**
     * Returns the value.
     */
    public Object value() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value value
     */
    public Result value(Object value) {
        this.value = value;
        return this;
    }

    /**
     * Returns the exception.
     */
    public EffiRpcException exception() {
        return exception;
    }

    /**
     * Sets the exception.
     *
     * @param exception exception
     */
    public Result exception(EffiRpcException exception) {
        this.exception = exception;
        return this;
    }

    /**
     * Checks if the result contains an exception.
     *
     * @return {@code true} if the result contains an exception, {@code false} otherwise.
     */
    public boolean hasException() {
        return exception != null;
    }

}

