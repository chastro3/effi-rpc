package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extensible;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Compresses and decompresses byte arrays using various compression algorithms.
 * <p>
 * Provides compression functionality for reducing data size during
 * transmission with extensible algorithm support.
 */
@Extensible(scope = PLATFORM)
public interface Compressor {

    void compress(OutputStream out, byte[] data) throws IOException;

    InputStream decompress(InputStream in) throws IOException;

}


