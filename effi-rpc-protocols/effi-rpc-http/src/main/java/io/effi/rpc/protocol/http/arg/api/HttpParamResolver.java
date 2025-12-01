package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.Argument;
import io.effi.rpc.context.parameter.ParamVar;
import io.effi.rpc.context.parameter.ParameterResolver;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Parameter;

/**
 * Parses the parameters from the URL of an HTTP request using the specified parameter variable.
 */
public class HttpParamResolver implements ParameterResolver<HttpRequest> {

    private final ParamVar<Argument.Source> paramVar;

    public HttpParamResolver(ParamVar<Argument.Source> paramVar) {
        this.paramVar = AssertUtil.notNull(paramVar, "paramVar");
    }

    @Override
    public Object resolve(HttpRequest request, Parameter parameter, Servant servant) {
        return request.url().getQueryParam(paramVar.get().get());
    }
}
