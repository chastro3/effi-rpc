package io.effi.rpc.serialization;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.marshalling.MarshallingErrorCodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Provides an abstract implementation of {@link Serializer}.
 */
public abstract class AbstractSerializer implements Serializer {

    @Override
    public void serialize(Object obj, OutputStream out) throws IOException {
        if (obj != null) {
            try {
                doSerialize(obj, out);
            } catch (EffiRpcException e) {
                throw e;
            } catch (IOException e) {
                throw MarshallingErrorCodes.SERIALIZE.fail(e, obj.getClass());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream in, Type type) throws IOException {
        if (in == null) return null;
        try {
            return (T) doDeserialize(in, type);
        } catch (EffiRpcException e) {
            throw e;
        } catch (IOException e) {
            throw MarshallingErrorCodes.DESERIALIZE.fail(e, type);
        }
    }

    protected abstract void doSerialize(Object obj, OutputStream out) throws IOException;

    protected abstract Object doDeserialize(InputStream in, Type type) throws IOException;

}

