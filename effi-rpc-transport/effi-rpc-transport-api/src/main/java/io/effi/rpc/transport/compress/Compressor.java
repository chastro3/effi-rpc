package io.effi.rpc.transport.compress;

import io.effi.rpc.spi.Extensible;

import static io.effi.rpc.constant.Component.Compression.GZIP;

/**
 * Compresses and decompresses byte arrays.
 */
@Extensible(GZIP)
public interface Compressor {

    /**
     * Compresses the input byte array.
     *
     * @param data the data to compress
     * @return the compressed data
     */
    byte[] compress(byte[] data);

    /**
     * Decompresses the input byte array.
     *
     * @param compressedData the data to decompress
     * @return the decompressed data
     */
    byte[] decompress(byte[] compressedData);

}


