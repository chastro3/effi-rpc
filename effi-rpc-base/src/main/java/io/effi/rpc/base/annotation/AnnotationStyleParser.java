package io.effi.rpc.base.annotation;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.annotation.spi.Extensible;
import io.effi.rpc.base.parameter.ParameterMapper;
import io.effi.rpc.base.parameter.ParameterParser;

import java.lang.reflect.Method;

/**
 * Parses annotation-based classes.
 */
@Extensible
public interface AnnotationStyleParser {

    /**
     * Parses annotations on the class.
     *
     * @param type the class to be parsed
     * @param config the configuration
     */
    void parseType(Class<?> type, NodeConfig config);

    /**
     * Parses annotations on the method.
     *
     * @param method the method to be parsed
     * @param config the configuration
     * @return updated configuration
     */
    NodeConfig parseMethod(Method method, NodeConfig config);

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



