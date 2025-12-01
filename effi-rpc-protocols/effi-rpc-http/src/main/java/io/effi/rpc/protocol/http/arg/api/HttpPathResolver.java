package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.Argument;
import io.effi.rpc.context.parameter.ParameterResolver;
import io.effi.rpc.context.parameter.PathVar;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Parameter;

/**
 * Parses the path variables from the URL of an HTTP request using the specified path variable configuration.
 */
public class HttpPathResolver implements ParameterResolver<HttpRequest> {

    private final PathVar<Argument.Source> pathVar;

    public HttpPathResolver(PathVar<Argument.Source> pathVar) {
        this.pathVar = AssertUtil.notNull(pathVar, "pathVar");
    }

    @Override
    public Object resolve(HttpRequest request, Parameter parameter, Servant servant) {
        return HttpUtil.findPathForVar(request.url(), pathVar.get().get(), servant);
    }
}
