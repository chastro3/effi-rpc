package io.effi.rpc.context;

import io.effi.rpc.config.SmartURL;

/**
 * Represents a messages containing request URL.
 * <p>
 * Provides a standardized interface for RPC messages with URL-based
 * addressing capabilities.
 */
public interface Message extends SmartURL.Supplier {

    /**
     * Returns the request URL.
     */
    @Override
    SmartURL url();

}


