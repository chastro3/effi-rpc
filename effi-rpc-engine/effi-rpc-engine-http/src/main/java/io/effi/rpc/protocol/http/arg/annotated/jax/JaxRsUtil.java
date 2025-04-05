package io.effi.rpc.protocol.http.arg.annotated.jax;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.parameter.Argument;
import io.effi.rpc.contract.parameter.Header;
import io.effi.rpc.contract.parameter.ParamVar;
import io.effi.rpc.contract.parameter.PathVar;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.netty.buffer.ByteBuf;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for handling JAX-RS-like annotations and extracting values from HTTP requests.
 */
public class JaxRsUtil {

    public static Argument wrapPath(Object arg, PathParam pathParam, Parameter parameter, Caller<?> caller) {
        return PathVar.target(Map.of(pathParam.value(), String.valueOf(arg)));
    }

    public static Argument wrapParam(Object arg, QueryParam queryParam, Parameter parameter, Caller<?> caller) {
        return ParamVar.target(Map.of(queryParam.value(), String.valueOf(arg)));
    }

    public static Argument wrapHeader(Object arg, HeaderParam headerParam, Parameter parameter, Caller<?> caller) {
        return Header.target(Map.of(headerParam.value(), String.valueOf(arg)));
    }

    /**
     * Retrieves the value for a path parameter from the request URL, or returns the default value if the parameter is not present.
     *
     * @param request   the HTTP request containing the URL
     * @param element   the annotation element
     * @param callee    the callee handling the request
     * @param pathParam the path parameter annotation
     * @return the value of the path parameter or the default value if not present
     */
    public static Object getPathOrDefault(HttpRequest<ByteBuf> request, PathParam pathParam, AnnotatedElement element, Callee<?> callee) {
        return getParameterOfDefault(element, () -> HttpUtil.findPathForVar(request.url(), pathParam.value(), callee));
    }

    /**
     * Retrieves the value for a query parameter from the request URL, or returns the default value if the parameter is not present.
     *
     * @param request    the HTTP request containing the URL
     * @param element    the annotation element
     * @param callee     the callee handling the request
     * @param queryParam the query parameter annotation
     * @return the value of the query parameter or the default value if not present
     */
    public static Object getParamOrDefault(HttpRequest<ByteBuf> request, QueryParam queryParam, AnnotatedElement element, Callee<?> callee) {
        return getParameterOfDefault(element, () -> HttpUtil.findParamForVar(request.url(), queryParam.value()));
    }

    /**
     * Retrieves the value for a header parameter from the request headers, or returns the default value if the header is not present.
     *
     * @param request     the HTTP request containing the headers
     * @param element     the annotation element
     * @param callee      the callee handling the request
     * @param headerParam the header parameter annotation
     * @return the value of the header parameter or the default value if not present
     */
    public static Object getHeaderOrDefault(HttpRequest<ByteBuf> request, HeaderParam headerParam, AnnotatedElement element, Callee<?> callee) {
        return getParameterOfDefault(element, () -> request.headers().get(headerParam.value()));
    }

    /**
     * Retrieves the value of a parameter and returns the default value if the value is null.
     *
     * @param element     the annotation element
     * @param valueGetter a supplier function to retrieve the value
     * @return the value or the default value if the value is null
     */
    public static Object getParameterOfDefault(AnnotatedElement element, Supplier<?> valueGetter) {
        Object value = valueGetter.get();
        return getValueOrDefault(value, element);
    }

    /**
     * Returns the value if it is not null, or the default value specified in the annotation if the value is null.
     *
     * @param value   the value to check
     * @param element the annotation element
     * @return the value or the default value if the value is null
     */
    public static Object getValueOrDefault(Object value, AnnotatedElement element) {
        if (value == null) {
            DefaultValue defaultValue = element.getAnnotation(DefaultValue.class);
            if (defaultValue != null) {
                return defaultValue.value();
            }
        }
        return value;
    }
}

