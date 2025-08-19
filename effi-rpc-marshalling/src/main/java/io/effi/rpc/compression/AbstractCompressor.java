package io.effi.rpc.compression;

import java.io.IOException;

/**
 * Provides an abstract implementation of {@link Compressor}.
 */
public abstract class AbstractCompressor implements Compressor {

    private static final byte[] EMPTY_ARRAY = new byte[0];

    @Override
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return EMPTY_ARRAY;
        }
        return doCompress(data);
    }

    @Override
    public byte[] decompress(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return EMPTY_ARRAY;
        }
        return doDecompress(compressedData);
    }

    protected abstract byte[] doCompress(byte[] data) throws IOException;

    protected abstract byte[] doDecompress(byte[] data) throws IOException;
}
