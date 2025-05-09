package io.effi.rpc.transport.compress;

import io.effi.rpc.spi.Extension;
import org.xerial.snappy.Snappy;

import java.io.IOException;

import static io.effi.rpc.constant.Component.Compression.SNAPPY;

/**
 * Implements {@link Compressor} using Snappy.
 */
@Extension(SNAPPY)
public class SnappyCompressor extends AbstractCompressor {

    @Override
    protected byte[] doCompress(byte[] data) throws IOException {
        return Snappy.compress(data);
    }

    @Override
    protected byte[] doDecompress(byte[] data) throws IOException {
        return Snappy.uncompress(data);
    }
}
