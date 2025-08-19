package io.effi.rpc.context.annotation;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.parameter.ParameterMapper;
import io.effi.rpc.context.parameter.ParameterParser;
import io.effi.rpc.config.HierarchicalConfig;

import java.lang.reflect.Method;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Parses annotation-based classes.
 */
@Extensible(scope = PLATFORM)
public interface AnnotationStyleParser {

    /**
     * Parses annotations on the class.
     *
     * @param type the class to be parsed
     * @param config the configuration
     */
    void parseType(Class<?> type, HierarchicalConfig config);

    /**
     * Parses annotations on the method.
     *
     * @param method the method to be parsed
     * @param config the configuration
     * @return updated configuration
     */
    HierarchicalConfig parseMethod(Method method, HierarchicalConfig config);

    /**
     * Parses the caller's parameter mapping.
     *
     * @param method the method for parameter mapping
     * @return an array of parameter mappers
     */
    ParameterMapper<AnnotationParameterWrapper<?>>[] parseCallerParameterMapper(Method method);

    /**
     * Parses the callee's parameter parsing mapping.
     *
     * @param method the method for parameter mapping
     * @return an array of parameter mappers
     */
    ParameterMapper<ParameterParser<?>>[] parseCalleeParameterMapper(Method method);

    /**
     * Checks if the method is supported.
     *
     * @param method the method to check
     * @return true if supported, false otherwise
     */
    boolean supported(Method method);
}



