package io.effi.rpc.base.annotation;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.parameter.ParameterParser;
import io.effi.rpc.util.AssertUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Parses method parameters annotated with a specific annotation from the given request.
 */
public class AnnotationParameterParser<T extends Annotation, REQ extends Message.Request> implements ParameterParser<REQ> {

    protected final Class<T> type;

    protected final AnnotationParameterParserHandler<T, REQ> handler;

    public AnnotationParameterParser(Class<T> type, AnnotationParameterParserHandler<T, REQ> handler) {
        this.type = AssertUtil.notNull(type, "type");
        this.handler = AssertUtil.notNull(handler, "handler");
    }

    @Override
    public Object parse(REQ request, Parameter parameter, Callee callee) {
        if (supported(parameter)) {
            T annotation = parameter.getAnnotation(type);
            return handler.handle(request, annotation, parameter, callee);
        }
        return null;
    }

    @Override
    public boolean supported(Parameter parameter) {
        return parameter.isAnnotationPresent(type);
    }

    @Override
    public String toString() {
        return "type=" + type.getName();
    }
}
