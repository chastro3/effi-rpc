package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extension;
import org.xerial.snappy.Snappy;

import java.io.IOException;

import static io.effi.rpc.config.ConfigValues.Compression.SNAPPY;

/**
 * Implements {@link Compressor} using Snappy.
 */
@Extension(value = SNAPPY, onClass = "org.xerial.snappy.Snappy")
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
