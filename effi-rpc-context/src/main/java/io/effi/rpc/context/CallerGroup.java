package io.effi.rpc.context;

/**
 * Wraps a client interface and manages its internal callers.
 */
public interface CallerGroup<T> extends PeerGroup<Caller<?>, T> {

    /**
     * Returns the client proxy exposed to callers.
     */
    T proxy();
}

