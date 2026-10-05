package io.effi.rpc.serialization.protobuf;

import com.google.protobuf.MessageLite;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.serialization.AbstractSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.serialization.protobuf.ProtobufSerializer.NAME;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} for Protobuf {@link MessageLite} payloads.
 * <p>
 * Only message types are supported; every other value is rejected.
 */
@Extension(value = NAME, onClass = "com.google.protobuf.MessageLite")
public class ProtobufSerializer extends AbstractSerializer {

    public static final String NAME = "protobuf";

    private final Map<Class<?>, MessageLite> messageMap = new ConcurrentHashMap<>();

    /**
     * Registers a default message that supplies the parser for its type.
     *
     * @param type           the message type
     * @param defaultMessage the default message used to resolve the parser
     * @param <T>            the message type
     */
    public <T extends MessageLite> void register(Class<T> type, T defaultMessage) {
        messageMap.put(type, defaultMessage);
    }

    @Override
    protected void doSerialize(Object obj, OutputStream out) throws IOException {
        if (obj instanceof MessageLite message) {
            message.writeTo(out);
        } else {
            throw new IOException("Only Support [com.google.protobuf.MessageLite] Type");
        }
    }

    @Override
    protected Object doDeserialize(InputStream in, Type type) throws IOException {
        if (type instanceof Class<?> classType && MessageLite.class.isAssignableFrom(classType)) {
            MessageLite messageLite = messageMap.get(classType);
            Object result;
            if (messageLite != null) {
                result = messageLite.getParserForType().parseFrom(in);
            } else {
                try {
                    Method parseFrom = classType.getDeclaredMethod("parseFrom", InputStream.class);
                    parseFrom.setAccessible(true);
                    result = parseFrom.invoke(null, in);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
            return result;
        } else {
            throw new IOException("Only Support [com.google.protobuf.MessageLite] Type");
        }
    }
}
