package io.effi.rpc.serialization;

import io.effi.rpc.common.spi.Extensible;

import java.lang.reflect.Type;

import static io.effi.rpc.common.constant.Component.Serialization.KRYO;

/**
 * Handles object serialization and deserialization.
 */
@Extensible(KRYO)
public interface Serializer {

    /**
     * Serializes the given object into a byte array.
     *
     * @param input The object to serialize.
     * @return A byte array representing the serialized object.
     */
    byte[] serialize(Object input);

    /**
     * Deserializes a byte array into an object of the specified type.
     *
     * @param bytes The byte array to deserialize.
     * @param type  The class of the object to deserialize.
     * @param <T>   The type of the object to deserialize.
     * @return The deserialized object of the specified type.
     */
    <T> T deserialize(byte[] bytes, Type type);
}


