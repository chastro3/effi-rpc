package io.effi.rpc.context.parameter;

import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;

import java.lang.reflect.Parameter;

/**
 * Resolves parameters from a request.
 */
public interface ParameterResolver<REQ extends Request> {

    /**
     * Resolves a parameter from the given request.
     *
     * @param request       the request
     * @param parameter the parameter to be parsed
     * @param servant    the callee handling the request
     * @return the parsed object
     */
    Object resolve(REQ request, Parameter parameter, Servant servant);

    /**
     * Checks if the parameter is supported by this parser.
     *
     * @param parameter the parameter to check
     * @return true if the parameter is supported, false otherwise
     */
    default boolean supports(Parameter parameter) {
        return true;
    }
}
