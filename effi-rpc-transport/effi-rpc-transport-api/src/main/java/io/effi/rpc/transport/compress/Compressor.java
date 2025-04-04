package io.effi.rpc.transport.compress;

import io.effi.rpc.common.spi.Extensible;

import java.io.IOException;

import static io.effi.rpc.common.constant.Component.Compression.GZIP;

/**
 * Handles data compression and decompression.
 */
@Extensible(GZIP)
public interface Compressor {

    /**
     * Compresses the given byte array.
     *
     * @param data the data to compress
     * @return the compressed data
     * @throws IOException if compression fails
     */
    byte[] compress(byte[] data) throws IOException;

    /**
     * Decompresses the given byte array.
     *
     * @param compressedData the data to decompress
     * @return the original uncompressed data
     * @throws IOException if decompression fails
     */
    byte[] decompress(byte[] compressedData) throws IOException;
}


