package io.effi.rpc.contract.annotation;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

@FunctionalInterface
public interface AnnotationParameterParserHandler<T extends Annotation, REQ extends Envelope.Request> {

    Object handle(REQ request, T annotation, Parameter parameter, Callee<?> callee);
}
