package io.effi.rpc.context.parameter;

import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Request;

/**
 * Resolves servant-side method arguments from a protocol request.
 */
@FunctionalInterface
public interface ParameterResolver {

    /**
     * Resolves one servant-side argument from the request.
     *
     * @param binding parameter binding
     * @param request protocol request
     * @param peer    target peer
     * @return resolved argument value
     */
    Object resolve(ParameterBinding binding, Request request, Peer peer);
}
