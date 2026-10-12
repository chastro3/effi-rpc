package io.effi.rpc.protocol.grpc;

import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.context.Peer;
import io.effi.rpc.marshalling.MarshallingErrorCodes;
import io.effi.rpc.protocol.grpc.stream.GrpcClientStream;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.util.StringUtil;
import io.netty.util.AttributeKey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Provides shared gRPC channel attributes and helpers.
 */
public final class GrpcSupport {

    public static final AttributeKey<GrpcClientStream> CLIENT_STREAM =
            AttributeKey.valueOf("effi-rpc.grpc.client-stream");

    private GrpcSupport() {
    }

    /**
     * Resolves the serializer configured for the supplied peer.
     *
     * @param peer target peer
     * @return configured serializer
     */
    public static Serializer serializer(Peer peer) {
        String name = peer.option(SerializationOptions.SERIALIZER);
        Serializer serializer = StringUtil.isNotBlank(name)
                ? peer.platform().namedExtension(Serializer.class, name)
                : null;
        return serializer != null ? serializer : peer.platform().preferredExtension(Serializer.class, null);
    }

    /**
     * Serializes one gRPC message payload.
     *
     * @param serializer payload serializer
     * @param value      message value
     * @return serialized payload
     */
    public static byte[] serialize(Serializer serializer, Object value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            serializer.serialize(value, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw MarshallingErrorCodes.SERIALIZE.fail(e, value == null ? null : value.getClass());
        }
    }

    /**
     * Deserializes one gRPC message payload.
     *
     * @param serializer payload serializer
     * @param payload    serialized payload
     * @param type       target type
     * @return deserialized value
     */
    public static Object deserialize(Serializer serializer, byte[] payload, Type type) {
        try {
            return serializer.deserialize(new ByteArrayInputStream(payload), type);
        } catch (IOException e) {
            throw MarshallingErrorCodes.DESERIALIZE.fail(e, type);
        }
    }
}
