package io.effi.rpc.context;

import io.effi.rpc.component.ScopedModule;

/**
 * Provides contextual data for an RPC call.
 * <p>
 * Encapsulates the context information available before request sending
 * on the client side or before method invocation on the server side.
 *
 * @param <R> the request message type
 * @param <P> the call side type
 */
public class CallContext<R extends Request, P extends Peer> extends Interaction.Context<R, P> {

    private final Object[] args;

    public CallContext(ScopedModule module, R request, P peer, Interaction.Mode<?> mode, Object[] args) {
        super(module, request, peer, mode);
        this.args = args;
    }

    /**
     * Returns the positional arguments shared by this call execution.
     * <p>
     * The array is intentionally mutable so call interceptors can adjust positional
     * arguments before the protocol creates the request; callers must not replace the
     * array reference.
     */
    public Object[] args() {
        return args;
    }
}


