package io.effi.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.serialization.AbstractSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import static io.effi.rpc.serialization.kryo.KryoSerializer.NAME;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using Kryo.
 */
@Extension(value = NAME, onClass = "com.esotericsoftware.kryo.Kryo", primary = true)
public class KryoSerializer extends AbstractSerializer {

    public static final String NAME = "kryo";

    // Set buffer size
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * Kryo is not thread safe. Each thread should have its own Kryo, Input, and Output instance.
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(false);
        return kryo;
    });

    @Override
    protected void doSerialize(Object obj, OutputStream out) throws IOException {
        try (Output output = new Output(out, BUFFER_SIZE)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeClassAndObject(output, obj);
        }
    }

    @Override
    protected Object doDeserialize(InputStream in, Type type) throws IOException {
        try (Input input = new Input(in)) {
            Kryo kryo = kryoThreadLocal.get();
            return kryo.readClassAndObject(input);
        }
    }
}
