package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Caller;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.protocol.http.HttpProtocol;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.transport.AbstractTransporter;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.TypeToken;

import java.net.InetSocketAddress;

import static io.effi.rpc.constant.Component.Protocol.HTTP_2;

/**
 * Implements {@link io.effi.rpc.transport.Protocol} using http2.
 */
@Extension(HTTP_2)
public class Http2Protocol extends HttpProtocol {

    public Http2Protocol() {
        super(HttpVersion.HTTP_2_0);
        transporter(new Transporter());
    }

    @Override
    public <T> Callee<T> createCallee(MethodMapper<T> methodMapper, NodeConfig config, EffiRpcModule module) {
        return Http2Callee.builder(methodMapper, config).module(module).build();
    }

    @Override
    public <T> Caller<T> createCaller(TypeToken<T> returnType, NodeConfig config, EffiRpcModule module) {
        return Http2Caller.builder(returnType, config).module(module).build();
    }

    private static final class Transporter extends AbstractTransporter {

        @Override
        protected Server newServer(ServerConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
            return new Http2Server(config, address, platform);
        }

        @Override
        protected Client newClient(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcPlatform platform) {
            return new Http2Client(config, remoteAddress, platform);
        }
    }
}
