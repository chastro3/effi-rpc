package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpProtocol;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.spi.Extension;
import io.effi.rpc.util.TypeToken;

import static io.effi.rpc.constant.Component.Protocol.HTTP;
import static io.effi.rpc.constant.Component.Protocol.HTTPS;

/**
 * Http protocol.
 */
@Extension({HTTP, HTTPS})
public class Http1Protocol extends HttpProtocol {

    public Http1Protocol() {
        super(HttpVersion.HTTP_1_1, Http1Transporter.INSTANCE);
    }

    public Http1Client getClient(String remoteAddress) {
        return (Http1Client) clients.get(remoteAddress);
    }

    @Override
    public <T> Callee<T> createCallee(MethodMapper<T> methodMapper, NodeConfig config, EffiRpcModule... modules) {
        return new Http1CalleeBuilder<T>(methodMapper, config).export(modules).build();
    }

    @Override
    public <T> Caller<T> createCaller(TypeToken<T> returnType, NodeConfig config, EffiRpcModule module) {
        return new Http1CallerBuilder<>(returnType, config).module(module).build();
    }
}
