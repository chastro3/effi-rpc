package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static io.effi.rpc.constant.Component.Compression.DEFLATE;

/**
 * Implements {@link Compressor} using Deflate.
 */
@Extension(DEFLATE)
public class DeflateCompressor extends AbstractCompressor {

    @Override
    protected byte[] doCompress(byte[] data) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (DeflaterOutputStream outputStream = new DeflaterOutputStream(byteOutput)) {
            outputStream.write(data);
        }
        return byteOutput.toByteArray();
    }

    @Override
    protected byte[] doDecompress(byte[] data) throws IOException {
        try (InflaterInputStream inputStream = new InflaterInputStream(new ByteArrayInputStream(data))) {
            return FileUtil.toBytes(inputStream);
        }
    }
}
