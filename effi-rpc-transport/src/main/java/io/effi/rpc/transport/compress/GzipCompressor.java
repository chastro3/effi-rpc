package io.effi.rpc.transport.compress;

import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.common.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static io.effi.rpc.common.constant.Component.Compression.GZIP;

/**
 * Based on the gzip compression algorithm.
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
            return FileUtil.inputStreamToByteArray(gzipInput);
        }
    }
}
