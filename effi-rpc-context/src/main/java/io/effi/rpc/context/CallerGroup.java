package io.effi.rpc.context;

/**
 * Wraps a client interface and manages its internal callers.
 */
public interface CallerGroup<T> extends PeerGroup<Caller<?>, T> {

    String proxy();
}

