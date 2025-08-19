package io.effi.rpc.transport.message;

import java.io.OutputStream;

/**
 * Represents transport-layer messages with output streams.
 * <p>
 * Provides output message functionality for writing message content
 * to output streams in transport layer communication.
 */
public interface OutputMessage extends IOMessage {

    /**
     * Returns the output stream for writing the message content.
     */
    OutputStream outputStream();
}
