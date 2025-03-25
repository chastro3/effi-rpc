package io.effi.rpc.transport.compress;

import io.effi.rpc.common.extension.spi.Extensible;

import java.io.IOException;

import static io.effi.rpc.common.constant.Component.Compression.GZIP;

/**
 * Compress and decompress data.
 */
@Extensible(GZIP)
public interface Compressor {

    /**
     * Compresses the provided byte array using the implemented compression algorithm.
     *
     * @param data the byte array to be compressed
     * @return the compressed byte array
     * @throws IOException if the compression fails for any reason,
     *                     such as invalid input data or issues
     *                     with the compression algorithm
     */
    byte[] compress(byte[] data) throws IOException;

    /**
     * Decompresses the provided compressed byte array using the implemented decompression algorithm.
     *
     * @param compressedData the byte array that was previously compressed
     * @return the original uncompressed byte array
     * @throws IOException if the decompression fails, such as when the
     *                     data is corrupted or incompatible with the
     *                     decompression algorithm
     */
    byte[] decompress(byte[] compressedData) throws IOException;
}

