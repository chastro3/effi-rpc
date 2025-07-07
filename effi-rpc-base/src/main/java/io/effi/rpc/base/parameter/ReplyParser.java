package io.effi.rpc.base.parameter;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;
import static io.effi.rpc.constant.Component.DEFAULT;

/**
 * Parses and converts response into result object.
 */
@Extensible(value = DEFAULT, scope = PLATFORM)
public interface ReplyParser<RESP extends Message.Response> {

    /**
     * Parses the given response and return a resolved result.
     * <p>
     * May involve deserialization and content transformation. The result is
     * suitable for downstream processing or returning to the original caller.
     * </p>
     *
     * @param response the response envelope to parse
     * @param caller   the caller that initiated the request
     * @return the resolved result
     */
    Result resolve(RESP response, Caller<?> caller);
}


