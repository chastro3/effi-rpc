package io.effi.rpc.compression;

import io.effi.rpc.exception.PredefinedErrorCode;

import java.io.IOException;

/**
 * Provides an abstract implementation of {@link Compressor}.
 */
public abstract class AbstractCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        try {
            return doCompress(data);
        } catch (IOException e) {
            throw PredefinedErrorCode.COMPRESS.fail(e);
        }
    }

    @Override
    public byte[] decompress(byte[] compressedData) {
        if (compressedData == null || compressedData.length == 0) {
            return new byte[0];
        }
        try {
            return doDecompress(compressedData);
        } catch (IOException e) {
            throw PredefinedErrorCode.DECOMPRESS.fail(e);
        }
    }

    protected abstract byte[] doCompress(byte[] data) throws IOException;

    protected abstract byte[] doDecompress(byte[] data) throws IOException;
}
