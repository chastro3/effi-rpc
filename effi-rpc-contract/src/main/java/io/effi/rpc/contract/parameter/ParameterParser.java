package io.effi.rpc.contract.parameter;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;

import java.lang.reflect.Parameter;

/**
 * Parse parameters from a request.
 *
 * @param <REQ> the type of the request
 */
public interface ParameterParser<REQ extends Envelope.Request> {

    /**
     * Parses a parameter from the given request.
     *
     * @param req       the request
     * @param parameter the parameter to be parsed
     * @param callee    the callee handling the request
     * @return the parsed object
     */
    Object parse(REQ req, Parameter parameter, Callee<?> callee);

    /**
     * Checks if the parameter is supported by this parser.
     *
     * @param parameter the parameter to check
     * @return true if the parameter is supported, false otherwise
     */
    default boolean supported(Parameter parameter) {
        return true;
    }
}
