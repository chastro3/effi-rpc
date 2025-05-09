package io.effi.rpc.serialization;

import io.effi.rpc.spi.Extensible;

import java.lang.reflect.Type;

import static io.effi.rpc.constant.Component.Serialization.KRYO;

/**
 * Serializes and deserializes objects.
 */
@Extensible(KRYO)
public interface Serializer {

    /**
     * Serializes the given object into a byte array.
     *
     * @param input the object to serialize
     * @return the serialized byte array
     */
    byte[] serialize(Object input);

    /**
     * Deserializes the given byte array into an object of the specified type.
     *
     * @param bytes the byte array to deserialize
     * @param type  the target type
     * @param <T>   the object type
     * @return the deserialized object
     */
    <T> T deserialize(byte[] bytes, Type type);
}


