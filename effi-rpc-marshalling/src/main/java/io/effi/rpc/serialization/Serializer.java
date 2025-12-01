package io.effi.rpc.serialization;

import io.effi.rpc.annotation.component.Extensible;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Serializes and deserializes objects using various serialization formats.
 * <p>
 * Provides serialization functionality for converting objects to and from
 * byte streams with platform-scoped extensibility.
 */
@Extensible(scope = PLATFORM)
public interface Serializer {

    /**
     * Serializes the given object to the output stream.
     *
     * @param obj the object to serialize
     * @param out the output stream to write to
     */
    void serialize(Object obj, OutputStream out) throws IOException;

    /**
     * Deserializes an object from the input stream with the given type.
     *
     * @param in the input stream to read from
     * @param type the target type to deserialize to
     * @return the deserialized object
     */
    <T> T deserialize(InputStream in, Type type) throws IOException;
}


