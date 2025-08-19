package io.effi.rpc.context;

/**
 * Represents RPC requests with reply requirement checking.
 * <p>
 * Extends the basic message interface to include request-specific
 * functionality for determining if a reply is needed.
 */
public interface Request extends Message {

    /**
     * Checks if a reply is required.
     */
    boolean needReply();
}