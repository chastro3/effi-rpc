package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.trait.Builder;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.context.ServantGroup;
import io.effi.rpc.context.parameter.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a MethodMapper for a specific method in a remote service.
 */
public class HttpServantMethodBuilder<T> implements Builder<ServantMethod<T>> {

    private final ServantGroup<T> service;

    private final String methodName;

    private final List<Mapping> argMapping = new ArrayList<>();

    public HttpServantMethodBuilder(ServantGroup<T> service, String methodName) {
        this.service = AssertUtil.notNull(service, "service");
        this.methodName = AssertUtil.notBlank(methodName, "method id");
    }

    public HttpServantMethodBuilder(ServantGroup<T> service,MethodRef ref){
        this.service = AssertUtil.notNull(service, "service");
        this.methodName = AssertUtil.notNull(ref, "method ref");
    }

    /**
     * Specifies the parameter type for mapping.
     */
    public HttpServantMethodBuilder<T> parameterType(Class<?> parameterType) {
        return mappedParameterType(parameterType, null);
    }

    /**
     * Maps a parameter type to an Argument.
     */
    public HttpServantMethodBuilder<T> mappedParameterType(Class<?> argType, Argument mappedArg) {
        Mapping mapping = new Mapping(argType, mappedArg);
        argMapping.add(mapping);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServantMethod<T> build() {
        Method method = validMethod();
        Parameter[] parameters = method.getParameters();
        ParameterBinding[] wrappers = new ParameterBinding[parameters.length];
        for (int i = 0; i < argMapping.size(); i++) {
            Argument arg = argMapping.get(i).mappedArg();
            ParameterResolver<?> resolver = null;
            if (arg instanceof PathVar<?> pathVar && pathVar.get() instanceof Argument.Source) {
                resolver = new HttpPathResolver((PathVar<Argument.Source>) pathVar);
            } else if (arg instanceof ParamVar<?> paramVar && paramVar.get() instanceof Argument.Source) {
                resolver = new HttpParamResolver((ParamVar<Argument.Source>) paramVar);
            } else if (arg instanceof Header<?> header && header.get() instanceof Argument.Source) {
                resolver = new HttpHeaderResolver((Header<Argument.Source>) header);
            } else if (arg instanceof Body<?> body) {
                resolver = new HttpBodyResolver(body);
            }
            wrappers[i] = new ParameterBinding(parameters[i], resolver);
        }
        return new ServantMethod<>(service, method, wrappers);
    }

    private Method validMethod() {
        Class<T> targetType = service.targetType();
        Class<?>[] parameterTypes = argMapping.stream().map(Mapping::argType).toArray(Class[]::new);
        try {
            return targetType.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Can't find method: " + e.getMessage(), e);
        }
    }

    private record Mapping(Class<?> argType, Argument mappedArg) {

    }
}
