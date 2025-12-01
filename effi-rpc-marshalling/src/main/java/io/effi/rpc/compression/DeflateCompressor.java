package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static io.effi.rpc.compression.DeflateCompressor.NAME;

/**
 * Implements {@link Compressor} using Deflate.
 */
@Extension(NAME)
public class DeflateCompressor implements Compressor {

    public static final String NAME = "deflate";

    @Override
    public void compress(OutputStream out, byte[] data) throws IOException {
        try (DeflaterOutputStream outputStream = new DeflaterOutputStream(out)) {
            outputStream.write(data);
        }
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        return new InflaterInputStream(in);
    }
}
