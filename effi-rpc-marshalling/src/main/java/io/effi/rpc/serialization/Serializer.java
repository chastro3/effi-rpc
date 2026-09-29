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

    /**
     * Serializes ordered values as one payload.
     */
    default void serializeValues(Object[] values, Type[] types, OutputStream out) throws IOException {
        serialize(values, out);
    }

    /**
     * Deserializes ordered values from one payload.
     *
     * <p>Self-describing serializers may use the default implementation.
     * Text or schema-constrained serializers should override this method to
     * deserialize each value directly using its declared type.
     *
     * @param in    the input stream
     * @param types the declared value types
     * @return the ordered values
     */
    default Object[] deserializeValues(InputStream in, Type[] types) throws IOException {
        return deserialize(in, Object[].class);
    }
}


