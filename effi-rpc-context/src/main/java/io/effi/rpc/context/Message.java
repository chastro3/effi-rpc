package io.effi.rpc.context;

import io.effi.rpc.config.SmartURL;

/**
 * Provides a message carrying its request URL.
 */
public interface Message extends SmartURL.Supplier {

    /**
     * Returns the request URL.
     */
    @Override
    SmartURL url();

}


