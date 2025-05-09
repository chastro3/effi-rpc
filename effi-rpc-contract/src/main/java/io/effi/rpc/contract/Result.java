package io.effi.rpc.contract;

import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLSource;

/**
 * Represents the result of an invocation.
 *
 * @see ResultType
 */
public interface Result extends URLSource {

    /**
     * Returns the result type.
     */
    ResultType<?> type();

    /**
     * Returns the value if present.
     */
    Object value();

    /**
     * Creates a typed result instance based on the value type.
     */
    static Result create(URL url, Object value) {
        return ResultType.resolve(url, value);
    }

    /**
     * Checks if this result represents an exception.
     */
    default boolean hasException() {
        return ResultType.EXCEPTION.match(this);
    }

    /**
     * Extracts a typed value from this result.
     */
    default <T> T as(ResultType<T> resultType) {
        return resultType.extract(this);
    }

}
