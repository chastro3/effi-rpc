package io.effi.rpc.contract.annotation;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Abstract implementation of {@link AnnotationStyleParser}.
 *
 * @param <REQ> the request type
 */
public abstract class AbstractAnnotationStyleParser<REQ extends Envelope.Request> implements AnnotationStyleParser {

    protected AnnotationConfigParser<Class<?>, ?>[] typeAnnotationConfigParsers;

    protected AnnotationConfigParser<Method, ?>[] methodAnnotationConfigParsers;

    protected AnnotationParameterParser<?, REQ>[] parameterParsers;

    protected AnnotationParameterWrapper<?>[] parameterWrappers;

    @SuppressWarnings("unchecked")
    protected AbstractAnnotationStyleParser() {
        this.typeAnnotationConfigParsers = typeConfigParsers().toArray(new AnnotationConfigParser[0]);
        this.methodAnnotationConfigParsers = methodConfigParsers().toArray(new AnnotationConfigParser[0]);
        this.parameterParsers = parameterParsers().toArray(new AnnotationParameterParser[0]);
        this.parameterWrappers = parameterWrappers().toArray(new AnnotationParameterWrapper[0]);
    }

    @Override
    public void parseType(Class<?> type, Config config) {
        if (CollectionUtil.isNotEmpty(typeAnnotationConfigParsers)) {
            for (AnnotationConfigParser<Class<?>, ?> parser : typeAnnotationConfigParsers) {
                parser.parse(type, config);
            }
        }
    }

    @Override
    public Config parseMethod(Method method, Config config) {
        if (CollectionUtil.isNotEmpty(methodAnnotationConfigParsers)) {
            for (AnnotationConfigParser<Method, ?> parser : methodAnnotationConfigParsers) {
                parser.parse(method, config);
            }
        }
        return config;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParameterMapper<AnnotationParameterWrapper<?>>[] parseCallerParameterMapper(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterMapper<AnnotationParameterWrapper<?>>[] parameterMappers = new ParameterMapper[parameters.length];
        loop:
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (CollectionUtil.isNotEmpty(parameterWrappers)) {
                for (AnnotationParameterWrapper<?> wrapper : parameterWrappers) {
                    if (wrapper.supported(parameter)) {
                        parameterMappers[i] = new ParameterMapper<>(parameter, wrapper);
                        continue loop;
                    }
                }
            }
            if (parameterMappers[i] == null) {
                parameterMappers[i] = new ParameterMapper<>(parameter, null);
            }
        }
        return parameterMappers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParameterMapper<ParameterParser<?>>[] parseCalleeParameterMapper(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterMapper<ParameterParser<?>>[] parameterMappers = new ParameterMapper[parameters.length];
        loop:
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (CollectionUtil.isNotEmpty(parameterWrappers)) {
                for (ParameterParser<REQ> parser : parameterParsers) {
                    if (parser.supported(parameter)) {
                        parameterMappers[i] = new ParameterMapper<>(parameter, parser);
                        continue loop;
                    }
                }
            }
            if (parameterMappers[i] == null) {
                parameterMappers[i] = new ParameterMapper<>(parameter, null);
            }
        }
        return parameterMappers;
    }

    protected abstract List<AnnotationConfigParser<Class<?>, ?>> typeConfigParsers();

    protected abstract List<AnnotationConfigParser<Method, ?>> methodConfigParsers();

    protected abstract List<AnnotationParameterWrapper<?>> parameterWrappers();

    protected abstract List<AnnotationParameterParser<?, REQ>> parameterParsers();

}
