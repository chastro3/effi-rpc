package io.effi.rpc.transport.codec;

import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.invocation.Invocation;

/**
 * Resolves protocol requests into protocol-neutral invocations.
 */
@FunctionalInterface
public interface InvocationResolver {

    Invocation resolve(Request request, Servant servant);
}
