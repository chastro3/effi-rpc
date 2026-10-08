package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.context.ServantGroup;
import io.effi.rpc.context.parameter.Argument;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.protocol.http.arg.binder.HttpParameterBinders;
import io.effi.rpc.trait.Builder;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Builds a MethodMapper for a specific method in a remote service.
 */
public class HttpServantMethodBuilder<T> implements Builder<ServantMethod<T>> {

    private final ServantGroup<T> service;

    private final String methodName;

    private Mapping[] mappings = new Mapping[0];

    private int mappingCount;

    public HttpServantMethodBuilder(ServantGroup<T> service, String methodName) {
        this.service = AssertUtil.notNull(service, "service");
        this.methodName = AssertUtil.notBlank(methodName, "method id");
    }

    /**
     * Maps a parameter type to an Argument.
     */
    public HttpServantMethodBuilder<T> mappedParameterType(Class<?> argType, Argument mappedArg) {
        if (mappingCount == mappings.length) {
            Mapping[] expanded = new Mapping[mappings.length == 0 ? 4 : mappings.length * 2];
            System.arraycopy(mappings, 0, expanded, 0, mappings.length);
            mappings = expanded;
        }
        mappings[mappingCount++] = new Mapping(argType, mappedArg);
        return this;
    }

    @Override
    public ServantMethod<T> build() {
        Method method = validMethod();
        Parameter[] parameters = method.getParameters();
        AssertUtil.valid(
                mappingCount == parameters.length,
                "mapped parameter count does not match method signature"
        );
        ParameterBinding[] bindings = new ParameterBinding[parameters.length];
        for (int i = 0; i < mappingCount; i++) {
            Argument argument = mappings[i].mappedArg();
            bindings[i] = new ParameterBinding(i, parameters[i], HttpParameterBinders.bind(argument));
        }
        return new ServantMethod<>(service, method, MethodBinding.of(method, bindings));
    }

    private Method validMethod() {
        Class<T> targetType = service.targetType();
        Class<?>[] parameterTypes = new Class<?>[mappingCount];
        for (int i = 0; i < mappingCount; i++) {
            parameterTypes[i] = mappings[i].argType();
        }
        try {
            return targetType.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Can't find method: " + e.getMessage(), e);
        }
    }

    private record Mapping(Class<?> argType, Argument mappedArg) {

    }
}
