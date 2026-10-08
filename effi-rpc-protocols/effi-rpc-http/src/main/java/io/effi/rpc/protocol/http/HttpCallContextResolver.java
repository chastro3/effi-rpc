package io.effi.rpc.protocol.http;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.MethodBinder;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.codec.CallContextResolver;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Resolves HTTP requests into server call contexts.
 */
public final class HttpCallContextResolver implements CallContextResolver {

    @Override
    public CallContext<Request, Servant> resolve(Request request, Servant servant) {
        HttpDuplexRequest httpRequest = (HttpDuplexRequest) request;
        MethodBinder methodBinder = servant.methodBinder();
        MethodBinding binding = methodBinder.binding();
        Object[] arguments = binding.positional()
                ? methodBinder.resolvePositional(decodedPositionalArguments(httpRequest, binding, servant))
                : methodBinder.resolve(request, servant);
        return new CallContext<>(servant.module(), request, servant, null, arguments);
    }

    private Object[] decodedPositionalArguments(HttpDuplexRequest request, MethodBinding binding, Servant servant) {
        Type[] types = new Type[binding.size()];
        for (int i = 0; i < binding.size(); i++) {
            types[i] = binding.parameter(i).parameter().getParameterizedType();
        }
        try {
            Serializer serializer = HttpUtil.serializer(servant.platform(), request);
            return decodePositionalArgs(request, serializer, types);
        } catch (IOException e) {
            throw TransportErrorCodes.DECODE.fail(e, request.getClass(), Object[].class);
        }
    }

    private Object[] decodePositionalArgs(HttpDuplexRequest request, Serializer serializer, Type[] types) throws IOException {
        return types.length == 0 || request.inputStream() == null
                ? new Object[0]
                : serializer.deserialize(request.inputStream(), types);
    }
}
