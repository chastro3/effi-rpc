package io.effi.rpc.serialization.jdk;

import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.serialization.AbstractSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

import static io.effi.rpc.common.constant.Component.Serialization.JDK;

/**
 * {@link io.effi.rpc.serialization.Serializer} implementation based on Java's built-in serialization.
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
