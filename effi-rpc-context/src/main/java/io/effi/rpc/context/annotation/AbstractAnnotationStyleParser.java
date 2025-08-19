package io.effi.rpc.context.annotation;

import io.effi.rpc.context.Request;
import io.effi.rpc.context.parameter.ParameterMapper;
import io.effi.rpc.context.parameter.ParameterParser;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.util.CollectionUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * todo 异步解析
 * Provides an abstract implementation of {@link AnnotationStyleParser}.
 */
public abstract class AbstractAnnotationStyleParser<REQ extends Request> implements AnnotationStyleParser {

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
    public void parseType(Class<?> type, HierarchicalConfig config) {
        if (CollectionUtil.isNotEmpty(typeAnnotationConfigParsers)) {
            for (AnnotationConfigParser<Class<?>, ?> parser : typeAnnotationConfigParsers) {
                parser.parse(type, config);
            }
        }
    }

    @Override
    public HierarchicalConfig parseMethod(Method method, HierarchicalConfig config) {
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
            if (CollectionUtil.isNotEmpty(parameterParsers)) {
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
