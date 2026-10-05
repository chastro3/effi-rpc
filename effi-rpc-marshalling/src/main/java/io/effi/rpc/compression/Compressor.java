package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extensible;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Compresses and decompresses payload bytes for transmission.
 * <p>
 * Implementations are platform-scoped extensions selected by compression name.
 */
@Extensible(scope = PLATFORM)
public interface Compressor {

    /**
     * Compresses the given bytes into the output stream.
     *
     * @param out  the output stream receiving compressed bytes
     * @param data the bytes to compress
     * @throws IOException if compression fails
     */
    void compress(OutputStream out, byte[] data) throws IOException;

    /**
     * Wraps the input stream with a decompressing stream.
     *
     * @param in the input stream containing compressed bytes
     * @return the decompressing stream
     * @throws IOException if the stream cannot be initialized
     */
    InputStream decompress(InputStream in) throws IOException;

}


