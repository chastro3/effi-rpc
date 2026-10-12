package io.effi.rpc.protocol.grpc;

import io.effi.rpc.protocol.grpc.stream.StreamObserver;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Defines the static contract of one gRPC method.
 * <p>
 * Resolves the call shape, message types, and argument layout once when the caller or
 * servant is built, so transport handlers never inspect the reflected method per call.
 */
public final class GrpcMethod {

    private final Type requestType;

    private final Type responseType;

    private final int requestIndex;

    private final int responseObserverIndex;

    private final int parameterCount;

    private final boolean requestObserver;

    public GrpcMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericTypes = method.getGenericParameterTypes();
        int requestIndex = -1;
        int responseObserverIndex = -1;
        int requestParameters = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (StreamObserver.class.isAssignableFrom(parameterTypes[i])) {
                responseObserverIndex = i;
                continue;
            }
            if (requestIndex < 0) {
                requestIndex = i;
            }
            requestParameters++;
        }
        this.parameterCount = parameterTypes.length;
        this.requestIndex = requestIndex;
        this.responseObserverIndex = responseObserverIndex;
        this.requestObserver = StreamObserver.class.isAssignableFrom(method.getReturnType());
        this.requestType = resolveRequestType(method, genericTypes);
        this.responseType = resolveResponseType(method, genericTypes);
        validate(method, requestParameters);
    }

    /**
     * Returns the request message type.
     */
    public Type requestType() {
        return requestType;
    }

    /**
     * Returns the reply message type.
     */
    public Type responseType() {
        return responseType;
    }

    /**
     * Returns whether the servant expects a response observer.
     */
    public boolean observesResponse() {
        return responseObserverIndex >= 0;
    }

    /**
     * Returns whether the servant returns a request observer and is invoked before the first message.
     */
    public boolean invokesAtHeaders() {
        return requestObserver;
    }

    /**
     * Builds the servant argument array for the supplied request message.
     *
     * @param request          request message, or {@code null} when the servant returns a request observer
     * @param responseObserver response observer, or {@code null} for a unary call
     * @return servant argument array
     */
    public Object[] arguments(Object request, StreamObserver<Object> responseObserver) {
        Object[] arguments = new Object[parameterCount];
        if (requestIndex >= 0) {
            arguments[requestIndex] = request;
        }
        if (responseObserverIndex >= 0) {
            arguments[responseObserverIndex] = responseObserver;
        }
        return arguments;
    }

    private Type resolveRequestType(Method method, Type[] genericTypes) {
        if (requestObserver) {
            Type returnType = method.getGenericReturnType();
            return returnType instanceof ParameterizedType parameterizedType
                    ? parameterizedType.getActualTypeArguments()[0]
                    : Object.class;
        }
        return requestIndex < 0 ? Object.class : genericTypes[requestIndex];
    }

    private Type resolveResponseType(Method method, Type[] genericTypes) {
        if (responseObserverIndex < 0) {
            return method.getGenericReturnType();
        }
        Type parameterType = genericTypes[responseObserverIndex];
        return parameterType instanceof ParameterizedType parameterizedType
                ? parameterizedType.getActualTypeArguments()[0]
                : Object.class;
    }

    private void validate(Method method, int requestParameters) {
        AssertUtil.valid(
                requestParameters <= 1,
                "gRPC method '{}' declares more than one request parameter",
                method
        );
        AssertUtil.valid(
                requestObserver || requestIndex >= 0,
                "gRPC method '{}' declares no request message",
                method
        );
        AssertUtil.valid(
                !requestObserver || responseObserverIndex >= 0,
                "gRPC method '{}' returns a request observer without a response observer parameter",
                method
        );
    }
}
