package io.effi.rpc.transport.compress;

import io.effi.rpc.spi.Extension;
import io.effi.rpc.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static io.effi.rpc.constant.Component.Compression.GZIP;

/**
 * Implements {@link Compressor} using Gzip.
 */
@Extension(GZIP)
public class GzipCompressor extends AbstractCompressor {

    @Override
    protected byte[] doCompress(byte[] data) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutput = new GZIPOutputStream(byteOutput)) {
            gzipOutput.write(data);
        }
        return byteOutput.toByteArray();
    }

    @Override
    protected byte[] doDecompress(byte[] data) throws IOException {
        try (GZIPInputStream gzipInput = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return FileUtil.toBytes(gzipInput);
        }
    }
}
