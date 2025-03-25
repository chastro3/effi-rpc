package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.common.extension.Builder;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.contract.RemoteService;
import io.effi.rpc.contract.parameter.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a MethodMapper for a specific method in a remote service.
 *
 * @param <T>
 */
public class HttpMethodMapperBuilder<T> implements Builder<MethodMapper<T>> {

    private final RemoteService<T> service;

    private final String methodName;

    private final List<Mapping> argMapping = new ArrayList<>();

    public HttpMethodMapperBuilder(RemoteService<T> service, String methodName) {
        this.service = AssertUtil.notNull(service, "service");
        this.methodName = AssertUtil.notBlank(methodName, "method name");
    }

    /**
     * Specifies the parameter type for mapping.
     *
     * @param parameterType the class of the parameter type
     * @return the current builder instance
     */
    public HttpMethodMapperBuilder<T> parameterType(Class<?> parameterType) {
        return mappedParameterType(parameterType, null);
    }

    /**
     * Maps a parameter type to an Argument.
     *
     * @param argType   the class of the argument type
     * @param mappedArg the argument to map to
     * @return the current builder instance
     */
    public HttpMethodMapperBuilder<T> mappedParameterType(Class<?> argType, Argument mappedArg) {
        Mapping mapping = new Mapping(argType, mappedArg);
        argMapping.add(mapping);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MethodMapper<T> build() {
        Method method = validMethod();
        Parameter[] parameters = method.getParameters();
        ParameterMapper<ParameterParser<?>>[] wrappers = new ParameterMapper[parameters.length];
        for (int i = 0; i < argMapping.size(); i++) {
            Argument arg = argMapping.get(i).mappedArg();
            ParameterParser<?> parser = null;
            if (arg instanceof PathVar<?> pathVar && pathVar.get() instanceof Argument.Source) {
                parser = new HttpPathParser((PathVar<Argument.Source>) pathVar);
            } else if (arg instanceof ParamVar<?> paramVar && paramVar.get() instanceof Argument.Source) {
                parser = new HttpParamParser((ParamVar<Argument.Source>) paramVar);
            } else if (arg instanceof Header<?> header && header.get() instanceof Argument.Source) {
                parser = new HttpHeaderParser((Header<Argument.Source>) header);
            } else if (arg instanceof Body<?> body) {
                parser = new HttpBodyParser(body);
            }
            wrappers[i] = new ParameterMapper<>(parameters[i], parser);
        }
        return new MethodMapper<>(service, method, wrappers);
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
