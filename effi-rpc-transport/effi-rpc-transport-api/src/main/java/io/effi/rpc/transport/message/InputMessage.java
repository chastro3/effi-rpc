package io.effi.rpc.transport.message;

import java.io.InputStream;

/**
 * Represents transport-layer messages with input streams.
 * <p>
 * Provides input message functionality for reading message content
 * from input streams in transport layer communication.
 */
public interface InputMessage extends IOMessage {

    /**
     * Returns the input stream for reading the message content.
     */
    InputStream inputStream();
}

