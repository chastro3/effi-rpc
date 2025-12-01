package io.effi.rpc;

import io.effi.rpc.compression.Compressor;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.util.AssertUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public class CompressibleSerializer implements Serializer {

    private final Serializer serializer;
    private final Compressor compressor;

    public CompressibleSerializer(Serializer serializer, Compressor compressor) {
        this.serializer = AssertUtil.notNull(serializer, "serializer");
        this.compressor = compressor;
    }

    @Override
    public void serialize(Object obj, OutputStream out) throws IOException {
        if (compressor == null) {
            serializer.serialize(obj, out);
        } else {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            serializer.serialize(obj, byteOut);
            compressor.compress(out, byteOut.toByteArray());
        }
    }

    @Override
    public <T> T deserialize(InputStream in, Type type) throws IOException {
        InputStream source = (compressor == null) ? in : compressor.decompress(in);
        return serializer.deserialize(source, type);
    }
}
