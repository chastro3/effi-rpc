package io.effi.rpc.serialization.jdk;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.serialization.AbstractSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import static io.effi.rpc.serialization.jdk.JdkSerializer.NAME;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using Jdk.
 */
@Extension(NAME)
public class JdkSerializer extends AbstractSerializer {

    public static final String NAME = "jdk";

    @Override
    protected void doSerialize(Object obj, OutputStream out) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
            objectOutputStream.writeObject(obj);
        }
    }

    @Override
    protected Object doDeserialize(InputStream in, Type type) throws IOException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
