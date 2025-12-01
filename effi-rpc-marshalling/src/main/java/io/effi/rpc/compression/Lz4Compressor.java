package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extension;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.effi.rpc.compression.Lz4Compressor.NAME;

/**
 * Implements {@link Compressor} using Lz4.
 */
@Extension(value = NAME, onClass = "net.jpountz.lz4.LZ4Factory")
public class Lz4Compressor implements Compressor {

    public static final String NAME = "lz4";

    private final LZ4Compressor compressor;

    private final LZ4FastDecompressor decompressor;

    public Lz4Compressor() {
        LZ4Factory factory = LZ4Factory.fastestInstance();
        compressor = factory.fastCompressor();
        decompressor = factory.fastDecompressor();
    }

    @Override
    public void compress(OutputStream out, byte[] data) throws IOException {
        try (LZ4BlockOutputStream outputStream = new LZ4BlockOutputStream(out, 1024, compressor)) {
            outputStream.write(data);
        }
    }

    @Override
    public InputStream decompress(InputStream in) throws IOException {
        return new LZ4BlockInputStream(in, decompressor);
    }
}
