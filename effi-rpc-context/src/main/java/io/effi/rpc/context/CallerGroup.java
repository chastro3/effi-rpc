package io.effi.rpc.context;

import io.effi.rpc.config.OptionName;

/**
 * Wraps a client interface and manages its internal callers.
 */
public interface CallerGroup<T> extends PeerGroup<Caller<?>, T> {

    OptionName<String> PROXY = OptionName.of("proxy");

    String proxy();
}

