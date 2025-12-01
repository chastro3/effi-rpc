package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extension;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.effi.rpc.compression.SnappyCompressor.NAME;

/**
 * Implements {@link Compressor} using Snappy.
 */
@Extension(value = NAME, onClass = "org.xerial.snappy.Snappy")
public class SnappyCompressor implements Compressor {

    public static final String NAME = "snappy";

    @Override
    public void compress(OutputStream out, byte[] data) throws IOException {
        try (SnappyOutputStream outputStream = new SnappyOutputStream(out)) {
            outputStream.write(data);
        }
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        return new SnappyInputStream(in);
    }
}
