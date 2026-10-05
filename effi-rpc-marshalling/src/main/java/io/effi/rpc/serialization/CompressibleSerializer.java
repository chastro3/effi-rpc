package io.effi.rpc.serialization;

import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.compression.Compressor;
import io.effi.rpc.marshalling.MarshallingErrorCodes;
import io.effi.rpc.option.Options;
import io.effi.rpc.util.AssertUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Decorates a serializer with optional compression and bounded decompression.
 */
public class CompressibleSerializer implements Serializer {

    private static final int BUFFER_SIZE = 2048;

    private final Serializer serializer;

    private final Compressor compressor;

    private final int maxDecompressedBytes;

    public CompressibleSerializer(Serializer serializer, Compressor compressor) {
        this(serializer, compressor, CompressionOptions.MAX_DECOMPRESSED_BYTES.defaultValue());
    }

    public CompressibleSerializer(Serializer serializer, Compressor compressor, Options options) {
        this(serializer, compressor, options.option(CompressionOptions.MAX_DECOMPRESSED_BYTES));
    }

    public CompressibleSerializer(Serializer serializer, Compressor compressor, int maxDecompressedBytes) {
        this.serializer = AssertUtil.notNull(serializer, "serializer");
        this.compressor = compressor;
        this.maxDecompressedBytes = maxDecompressedBytes;
    }

    @Override
    public void serialize(Object obj, OutputStream out) throws IOException {
        if (compressor == null || obj == null) {
            serializer.serialize(obj, out);
            return;
        }
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        serializer.serialize(obj, byteOut);
        try {
            compressor.compress(out, byteOut.toByteArray());
        } catch (IOException e) {
            throw MarshallingErrorCodes.COMPRESS.fail(e);
        }
    }

    @Override
    public <T> T deserialize(InputStream in, Type type) throws IOException {
        if (compressor == null) {
            return serializer.deserialize(in, type);
        }
        byte[] data;
        try {
            data = readBounded(compressor.decompress(in));
        } catch (IOException e) {
            throw MarshallingErrorCodes.DECOMPRESS.fail(e);
        }
        return serializer.deserialize(new ByteArrayInputStream(data), type);
    }

    private byte[] readBounded(InputStream in) throws IOException {
        try (in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int total = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                // A non-positive limit disables the decompression bound.
                if (maxDecompressedBytes > 0 && total > maxDecompressedBytes) {
                    throw new IOException("Decompressed payload exceeds the configured limit of " + maxDecompressedBytes + " bytes");
                }
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }
}
