package io.effi.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.effi.rpc.serialization.AbstractSerializer;
import io.effi.rpc.spi.Extension;

import java.lang.reflect.Type;

import static io.effi.rpc.constant.Component.Serialization.KRYO;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using Kryo.
 */
@Extension(KRYO)
public class KryoSerializer extends AbstractSerializer {

    // Set buffer size
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * Kryo is not thread safe. Each thread should have its own Kryo, Input, and Output instances.
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(false);
        return kryo;
    });

    @Override
    protected byte[] doSerialize(Object input) throws Exception {
        try (Output output = new Output(BUFFER_SIZE, -1)) { // 使用自定义的缓冲区大小
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeClassAndObject(output, input);
            return output.toBytes();
        }
    }

    @Override
    protected Object doDeserialize(byte[] bytes, Type type) throws Exception {
        try (Input input = new Input(bytes)) {
            Kryo kryo = kryoThreadLocal.get();
            return kryo.readClassAndObject(input);
        }
    }

}
