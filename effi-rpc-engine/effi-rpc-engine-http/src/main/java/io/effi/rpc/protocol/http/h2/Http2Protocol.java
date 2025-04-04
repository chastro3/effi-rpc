package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpProtocol;
import io.effi.rpc.protocol.http.support.HttpVersion;

import static io.effi.rpc.common.constant.Component.Protocol.H2;
import static io.effi.rpc.common.constant.Component.Protocol.H2C;

/**
 * Http2 Protocol.
 */
@Extension({H2, H2C})
public class Http2Protocol extends HttpProtocol {

    public Http2Protocol() {
        super(HttpVersion.HTTP_2_0, new Http2Transporter());
    }

    @Override
    public <T> Callee<T> createCallee(MethodMapper<T> methodMapper, Config config, EffiRpcModule... modules) {
        return new Http2CalleeBuilder<T>(methodMapper, config).export(modules).build();
    }

    @Override
    public <T> Caller<T> createCaller(TypeToken<T> returnType, Config config, EffiRpcModule module) {
        return new Http2CallerBuilder<>(returnType, config).module(module).build();
    }
}
