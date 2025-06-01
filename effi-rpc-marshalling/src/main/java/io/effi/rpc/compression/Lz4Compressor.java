package io.effi.rpc.compression;

import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.util.FileUtil;
import net.jpountz.lz4.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.effi.rpc.constant.Component.Compression.LZ4;

/**
 * Implements {@link Compressor} using Lz4.
 */
@Extension(value = LZ4, onClass = "net.jpountz.lz4.LZ4Factory")
public class Lz4Compressor extends AbstractCompressor {

    private final LZ4Compressor compressor;

    private final LZ4FastDecompressor decompressor;

    public Lz4Compressor() {
        LZ4Factory factory = LZ4Factory.fastestInstance();
        compressor = factory.fastCompressor();
        decompressor = factory.fastDecompressor();
    }

    @Override
    protected byte[] doCompress(byte[] data) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (LZ4BlockOutputStream outputStream = new LZ4BlockOutputStream(byteOutput, 1024, compressor)) {
            outputStream.write(data);
        }
        return byteOutput.toByteArray();
    }

    @Override
    protected byte[] doDecompress(byte[] data) throws IOException {
        try (LZ4BlockInputStream inputStream = new LZ4BlockInputStream(new ByteArrayInputStream(data), decompressor)) {
            return FileUtil.toBytes(inputStream);
        }
    }
}
