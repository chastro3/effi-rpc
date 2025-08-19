package io.effi.rpc.compression;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static io.effi.rpc.config.ConfigValues.Compression.GZIP;

/**
 * Implements {@link Compressor} using Gzip.
 */
@Extension(GZIP)
public class GzipCompressor extends AbstractCompressor {

    @Override
    protected byte[] doCompress(byte[] data) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (GZIPOutputStream outputStream = new GZIPOutputStream(byteOutput)) {
            outputStream.write(data);
        }
        return byteOutput.toByteArray();
    }

    @Override
    protected byte[] doDecompress(byte[] data) throws IOException {
        try (GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return FileUtil.toBytes(inputStream);
        }
    }
}
