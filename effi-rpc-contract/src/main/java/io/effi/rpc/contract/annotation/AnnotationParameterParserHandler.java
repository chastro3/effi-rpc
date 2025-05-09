package io.effi.rpc.contract.annotation;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Handles parameter extraction based on a specific annotation.
 */
@FunctionalInterface
public interface AnnotationParameterParserHandler<T extends Annotation, REQ extends Envelope.Request> {

    /**
     * Extracts the parameter value using the annotation metadata.
     *
     * @param request    the incoming request
     * @param annotation the annotation instance
     * @param parameter  the method parameter
     * @param callee     the callee context
     * @return the extracted parameter value, or {@code null} if not supported
     */
    Object handle(REQ request, T annotation, Parameter parameter, Callee<?> callee);
}
