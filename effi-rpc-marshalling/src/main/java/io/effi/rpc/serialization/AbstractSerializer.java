package io.effi.rpc.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Provides an abstract implementation of {@link Serializer}.
 */
public abstract class AbstractSerializer implements Serializer {

    @Override
    public void serialize(Object obj, OutputStream out) throws IOException {
        if (obj != null) {
            doSerialize(obj, out);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream in, Type type) throws IOException {
        if (in == null) return null;
        return (T) doDeserialize(in, type);
    }

    protected abstract void doSerialize(Object obj, OutputStream out) throws IOException;

    protected abstract Object doDeserialize(InputStream in, Type type) throws IOException;

}

