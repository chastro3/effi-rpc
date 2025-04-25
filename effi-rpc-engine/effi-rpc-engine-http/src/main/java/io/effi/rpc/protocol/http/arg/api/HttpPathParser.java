package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.parameter.Argument;
import io.effi.rpc.contract.parameter.ParameterParser;
import io.effi.rpc.contract.parameter.PathVar;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Parameter;

/**
 * Parses the path variables from the URL of an HTTP request using the specified path variable configuration.
 */
public class HttpPathParser implements ParameterParser<HttpRequest<ByteBuf>> {

    private final PathVar<Argument.Source> pathVar;

    public HttpPathParser(PathVar<Argument.Source> pathVar) {
        this.pathVar = AssertUtil.notNull(pathVar, "pathVar");
    }

    @Override
    public Object parse(HttpRequest<ByteBuf> request, Parameter parameter, Callee<?> callee) {
        return HttpUtil.findPathForVar(request.url(), pathVar.get().get(), callee);
    }
}
