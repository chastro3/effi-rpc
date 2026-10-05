package io.effi.rpc.transport.message;

import java.io.OutputStream;

/**
 * Represents a transport-layer message carrying an output stream.
 */
public interface OutputMessage extends IOMessage {

    /**
     * Returns the output stream for writing the message content.
     */
    OutputStream outputStream();
}
