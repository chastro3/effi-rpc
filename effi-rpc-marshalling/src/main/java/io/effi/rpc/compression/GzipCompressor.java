package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static io.effi.rpc.compression.GzipCompressor.NAME;

/**
 * Implements {@link Compressor} using Gzip.
 */
@Extension(value = NAME, primary = true)
public class GzipCompressor implements Compressor {

    public static final String NAME = "gzip";

    @Override
    public void compress(OutputStream out, byte[] data) throws IOException {
        try (GZIPOutputStream outputStream = new GZIPOutputStream(out)) {
            outputStream.write(data);
        }
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        return new GZIPInputStream(in);
    }
}
