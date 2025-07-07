package io.effi.rpc.protocol.http.arg.api;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.parameter.Argument;
import io.effi.rpc.base.parameter.ParamVar;
import io.effi.rpc.base.parameter.ParameterParser;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.util.AssertUtil;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Parameter;

/**
 * Parses the parameters from the URL of an HTTP request using the specified parameter variable.
 */
public class HttpParamParser implements ParameterParser<HttpRequest<ByteBuf>> {

    private final ParamVar<Argument.Source> paramVar;

    public HttpParamParser(ParamVar<Argument.Source> paramVar) {
        this.paramVar = AssertUtil.notNull(paramVar, "paramVar");
    }

    @Override
    public Object parse(HttpRequest<ByteBuf> request, Parameter parameter, Callee callee) {
        return HttpUtil.findParamForVar(request.url(), paramVar.get().get());
    }
}
