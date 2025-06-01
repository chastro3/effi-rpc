package io.effi.rpc.serialization.jdk;

import io.effi.rpc.serialization.AbstractSerializer;
import io.effi.rpc.annotation.spi.Extension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

import static io.effi.rpc.constant.Component.Serialization.JDK;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using Jdk.
 */
@Extension(JDK)
public class JdkSerializer extends AbstractSerializer {

    @Override
    protected byte[] doSerialize(Object input) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(input);
            return outputStream.toByteArray();
        }
    }

    @Override
    protected Object doDeserialize(byte[] bytes, Type type) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return objectInputStream.readObject();
        }
    }

}
