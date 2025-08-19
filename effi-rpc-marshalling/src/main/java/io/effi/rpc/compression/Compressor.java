package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extensible;

import java.io.IOException;

import static io.effi.rpc.config.ConfigValues.Compression.GZIP;

/**
 * Compresses and decompresses byte arrays using various compression algorithms.
 * <p>
 * Provides compression functionality for reducing data size during
 * transmission with extensible algorithm support.
 */
@Extensible(GZIP)
public interface Compressor {

    /**
     * Compresses the input byte array.
     *
     * @param data the data to compression
     * @return the compressed data
     */
    byte[] compress(byte[] data) throws IOException;

    /**
     * Decompresses the input byte array.
     *
     * @param compressedData the data to decompress
     * @return the decompressed data
     */
    byte[] decompress(byte[] compressedData) throws IOException;

}


