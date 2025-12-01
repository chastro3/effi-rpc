package io.effi.rpc.context.annotation;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.parameter.ParameterResolver;
import io.effi.rpc.util.AssertUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Parses method parameters annotated with a specific annotation from the given request.
 */
public class AnnotationParameterResolver<T extends Annotation, REQ extends Request> implements ParameterResolver<REQ> {

    protected final Class<T> type;

    protected final AnnotationParameterParserHandler<T, REQ> handler;

    public AnnotationParameterResolver(Class<T> type, AnnotationParameterParserHandler<T, REQ> handler) {
        this.type = AssertUtil.notNull(type, "type");
        this.handler = AssertUtil.notNull(handler, "handler");
    }

    @Override
    public Object resolve(REQ request, Parameter parameter, Servant servant) {
        if (supports(parameter)) {
            T annotation = parameter.getAnnotation(type);
            return handler.handle(request, annotation, parameter, servant);
        }
        return null;
    }

    @Override
    public boolean supports(Parameter parameter) {
        return parameter.isAnnotationPresent(type);
    }

    @Override
    public String toString() {
        return "type=" + type.getName();
    }
}
