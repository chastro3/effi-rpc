package io.effi.rpc.contract.annotation;

import io.effi.rpc.common.extension.spi.Extensible;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;

import java.lang.reflect.Method;

/**
 * 解析注解风格的类
 */
@Extensible
public interface AnnotationStyleParser {

    /**
     * 解析类上的注解
     *
     * @param type
     * @param config
     */
    void parseType(Class<?> type, Config config);

    /**
     * 解析方法上的注解
     *
     * @param method
     * @param config
     * @return
     */
    Config parseMethod(Method method, Config config);

    /**
     * 解析调用方的参数包装器映射器
     *
     * @param method
     * @return
     */
    ParameterMapper<AnnotationParameterWrapper<?>>[] parseCallerParameterMapper(Method method);

    /**
     * 解析被调用方的参数解析映射器
     *
     * @param method
     * @return
     */
    ParameterMapper<ParameterParser<?>>[] parseCalleeParameterMapper(Method method);

    /**
     * 是否支持该方法
     *
     * @param method
     * @return
     */
    boolean supported(Method method);
}


