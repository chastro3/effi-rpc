package io.effi.rpc.transport.compress;

import java.io.IOException;

/**
 * Abstract implementation of {@link Compressor}.
 */
public abstract class AbstractCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        return doCompress(data);
    }

    @Override
    public byte[] decompress(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return new byte[0];
        }
        return doDecompress(compressedData);
    }

    protected abstract byte[] doCompress(byte[] data) throws IOException;

    protected abstract byte[] doDecompress(byte[] data) throws IOException;
}
