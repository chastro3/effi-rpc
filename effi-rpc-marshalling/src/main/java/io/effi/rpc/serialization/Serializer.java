package io.effi.rpc.serialization;

import io.effi.rpc.annotation.component.Extensible;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Serializes and deserializes objects between Java types and byte streams.
 * <p>
 * Serializers are platform-scoped extensions selected by serializer name.
 */
@Extensible(scope = PLATFORM)
public interface Serializer {

    /**
     * Serializes the given object to the output stream.
     *
     * @param obj the object to serialize
     * @param out the output stream to write to
     * @throws IOException if serialization fails
     */
    void serialize(Object obj, OutputStream out) throws IOException;

    /**
     * Deserializes an object from the input stream with the given type.
     *
     * @param in   the input stream to read from
     * @param type the target type to deserialize to
     * @param <T>  the deserialized type
     * @return the deserialized object
     * @throws IOException if deserialization fails
     */
    <T> T deserialize(InputStream in, Type type) throws IOException;

    /**
     * Deserializes ordered values from one payload.
     * <p>
     * Self-describing serializers may use the default implementation. Text or
     * schema-constrained serializers should override this method to deserialize
     * each value with its declared type.
     *
     * @param in    the input stream
     * @param types the declared value types
     * @return the ordered values
     * @throws IOException if deserialization fails
     */
    default Object[] deserialize(InputStream in, Type[] types) throws IOException {
        return deserialize(in, Object[].class);
    }
}


