package io.effi.rpc.base.annotation;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Handles parameter extraction based on a specific annotation.
 */
@FunctionalInterface
public interface AnnotationParameterParserHandler<T extends Annotation, REQ extends Message.Request> {

    /**
     * Extracts the parameter value using the annotation metadata.
     *
     * @param request    the incoming request
     * @param annotation the annotation instance
     * @param parameter  the method parameter
     * @param callee     the callee context
     * @return the extracted parameter value, or {@code null} if not supported
     */
    Object handle(REQ request, T annotation, Parameter parameter, Callee callee);
}
