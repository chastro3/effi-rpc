package io.effi.rpc.base;

import io.effi.rpc.config.URL;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;

import java.util.function.Function;

/**
 * Describes result type.
 */
public final class ResultType<T> {

    public static final ResultType<Object> VALUE = new ResultType<>(val -> true);

    public static final ResultType<ReplyFuture> FUTURE = new ResultType<>(val -> val instanceof ReplyFuture);

    public static final ResultType<EffiRpcException> EXCEPTION = new ResultType<>(val -> val instanceof EffiRpcException);

    private final Function<Object, Boolean> matcher;

    private ResultType(Function<Object, Boolean> matcher) {
        this.matcher = matcher;
    }

    public static Result resolve(URL url, Object value) {
        if (EXCEPTION.match(value)) {
            return EXCEPTION.createResult(url, (EffiRpcException) value);
        } else if (FUTURE.match(value)) {
            return FUTURE.createResult(url, (ReplyFuture) value);
        } else {
            return VALUE.createResult(url, value);
        }
    }

    public boolean match(Result result) {
        return match(result.value());
    }

    public boolean match(Object value) {
        return matcher.apply(value);
    }

    public Result createResult(URL url, T value) {
        return new DefaultResult(url, this, value);
    }

    @SuppressWarnings("unchecked")
    public T extract(Result result) {
        Object val = result.value();
        if (!match(val)) {
            return null;
        }
        return (T) val;
    }

    record DefaultResult(URL url, ResultType<?> type, Object value) implements Result {

        DefaultResult(URL url, ResultType<?> type, Object value) {
            this.url = AssertUtil.notNull(url, "url");
            this.type = AssertUtil.notNull(type, "type");
            this.value = value;
        }
    }

}
