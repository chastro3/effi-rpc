package io.effi.rpc.contract.annotation;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.parameter.ParameterParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Parses method parameters annotated with a specific annotation from the given request.
 *
 * @param <T>   the type of annotation to support
 * @param <REQ> the request type from which parameters are parsed
 */
public class AnnotationParameterParser<T extends Annotation, REQ extends Envelope.Request> implements ParameterParser<REQ> {

    protected final Class<T> type;

    protected final AnnotationParameterParserHandler<T, REQ> handler;

    public AnnotationParameterParser(Class<T> type, AnnotationParameterParserHandler<T, REQ> handler) {
        this.type = AssertUtil.notNull(type, "type");
        this.handler = AssertUtil.notNull(handler, "handler");
    }

    @Override
    public Object parse(REQ request, Parameter parameter, Callee<?> callee) {
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
