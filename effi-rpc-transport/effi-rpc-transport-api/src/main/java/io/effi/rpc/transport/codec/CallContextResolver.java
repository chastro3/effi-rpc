package io.effi.rpc.transport.codec;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;

/**
 * Resolves protocol requests into server call contexts.
 */
@FunctionalInterface
public interface CallContextResolver {

    /**
     * Resolves the server call context from the supplied request and servant.
     *
     * @param request protocol request
     * @param servant target servant
     * @return resolved call context
     */
    CallContext<Request, Servant> resolve(Request request, Servant servant);
}
