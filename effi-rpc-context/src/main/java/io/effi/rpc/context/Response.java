package io.effi.rpc.context;

import io.effi.rpc.exception.EffiRpcException;

/**
 * Defines an RPC response with success status and error information.
 * <p>
 * Extends the basic message interface to include response-specific
 * functionality for checking success status and retrieving failure causes.
 */
public interface Response extends Message {

    /**
     * Checks if the response is successful.
     */
    boolean succeeded();

    /**
     * Returns the cause of the failure.
     */
    EffiRpcException cause();
}
