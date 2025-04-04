package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpProtocol;
import io.effi.rpc.protocol.http.support.HttpVersion;

import static io.effi.rpc.common.constant.Component.Protocol.HTTP;
import static io.effi.rpc.common.constant.Component.Protocol.HTTPS;

/**
 * Http protocol.
 */
@Extension({HTTP, HTTPS})
public class Http1Protocol extends HttpProtocol {

    public Http1Protocol() {
        super(HttpVersion.HTTP_1_1, new Http1Transporter());
    }

    public Http1Client acquireClient(ClientConfig config) {
        String key = config.config().get(KeyConstant.NAME, config.protocol());
        return (Http1Client) clients.get(key);
    }

    @Override
    public <T> Callee<T> createCallee(MethodMapper<T> methodMapper, Config config, EffiRpcModule... modules) {
        return new Http1CalleeBuilder<T>(methodMapper, config).export(modules).build();
    }

    @Override
    public <T> Caller<T> createCaller(TypeToken<T> returnType, Config config, EffiRpcModule module) {
        return new Http1CallerBuilder<>(returnType, config).module(module).build();
    }
}
