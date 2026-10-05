package io.effi.rpc.transport.message;

import java.io.InputStream;

/**
 * Represents a transport-layer message carrying an input stream.
 */
public interface InputMessage extends IOMessage {

    /**
     * Returns the input stream for reading the message content.
     */
    InputStream inputStream();
}

