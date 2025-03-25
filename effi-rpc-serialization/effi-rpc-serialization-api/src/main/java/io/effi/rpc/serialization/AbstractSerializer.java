package io.effi.rpc.serialization;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;

import java.lang.reflect.Type;

/**
 * Abstract implementation of {@link Serializer}.
 */
public abstract class AbstractSerializer implements Serializer {

    @Override
    public byte[] serialize(Object input) {
        if (input == null) {
            return new byte[0];
        }
        try {
            return doSerialize(input);
        } catch (Exception e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.SERIALIZE, e, input.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Type type) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return (T) doDeserialize(bytes, type);
        } catch (Exception e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.DESERIALIZE, e, type);
        }
    }

    protected abstract byte[] doSerialize(Object input) throws Exception;

    protected abstract Object doDeserialize(byte[] bytes, Type type) throws Exception;

}

