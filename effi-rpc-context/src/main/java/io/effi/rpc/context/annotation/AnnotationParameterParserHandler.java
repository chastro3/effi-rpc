package io.effi.rpc.context.annotation;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Handles parameter extraction based on a specific annotation.
 */
@FunctionalInterface
public interface AnnotationParameterParserHandler<T extends Annotation, REQ extends Request> {

    /**
     * Extracts the parameter value using the annotation metadata.
     *
     * @param request    the incoming request
     * @param annotation the annotation instance
     * @param parameter  the method parameter
     * @param servant     the callee context
     * @return the extracted parameter value, or {@code null} if not supported
     */
    Object handle(REQ request, T annotation, Parameter parameter, Servant servant);
}
