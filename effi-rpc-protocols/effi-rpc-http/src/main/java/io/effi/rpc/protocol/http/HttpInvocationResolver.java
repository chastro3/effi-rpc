package io.effi.rpc.protocol.http;

import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.invocation.InvocationArguments;
import io.effi.rpc.context.invocation.InvocationAttributes;
import io.effi.rpc.context.invocation.InvocationKeys;
import io.effi.rpc.context.invocation.PositionalInvocation;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.codec.InvocationResolver;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Resolves HTTP requests into protocol-neutral invocations.
 */
public final class HttpInvocationResolver implements InvocationResolver {

    @Override
    public Invocation resolve(Request request, Servant servant) {
        HttpDuplexRequest httpRequest = (HttpDuplexRequest) request;
        InvocationAttributes attributes = new InvocationAttributes();
        attributes.set(InvocationKeys.MODULE, servant.module());
        attributes.set(HttpInvocationKeys.REQUEST, httpRequest);
        attributes.set(HttpInvocationKeys.PATH_TEMPLATE, servant.queryPath());
        return new PositionalInvocation(positionalArguments(httpRequest, servant), attributes);
    }

    private InvocationArguments positionalArguments(HttpDuplexRequest request, Servant servant) {
        MethodBinding binding = servant.methodBinder().binding();
        if (!binding.positional()) {
            return new InvocationArguments(0);
        }
        Type[] types = new Type[binding.size()];
        for (int i = 0; i < binding.size(); i++) {
            types[i] = binding.parameter(i).parameter().getParameterizedType();
        }
        try {
            Serializer serializer = HttpUtil.serializer(servant.platform(), request);
            return new InvocationArguments(decodePositionalArgs(request, serializer, types));
        } catch (IOException e) {
            throw TransportErrorCodes.DECODE.fail(e, request.getClass(), Object[].class);
        }
    }

    private Object[] decodePositionalArgs(HttpDuplexRequest request, Serializer serializer, Type[] types) throws IOException {
        return types.length == 0 || request.inputStream() == null
                ? new Object[0]
                : serializer.deserializeValues(request.inputStream(), types);
    }
}
