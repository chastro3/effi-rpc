package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.protocol.http.HttpProtocol;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.TypeCapture;

import java.net.InetSocketAddress;
import java.util.Map;

import static io.effi.rpc.config.ConfigValues.Protocol.HTTP_2;

/**
 * Implements {@link TransportProtocol} using http2.
 */
@Extension(HTTP_2)
public class Http2Protocol extends HttpProtocol {

    public static final HttpVersion VERSION = new Version();

    public Http2Protocol() {
        super(VERSION);
    }

    @Override
    public <T> Callee createCallee(MethodMapper<T> methodMapper, HierarchicalConfig config, ScopedModule module) {
        return Http2Callee.builder(methodMapper, config).module(module).build();
    }

    @Override
    public <T> Caller<T> createCaller(TypeCapture<T> returnType, HierarchicalConfig config, ScopedModule module) {
        return Http2Caller.builder(returnType, config).module(module).build();
    }

    @Override
    protected Server createServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
        return new Http2Server(config, address, platform);
    }

    @Override
    protected Client createClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform) {
        return new Http2Client(config, remoteAddress, platform);
    }

    private static final class Version implements HttpVersion {

        @Override
        public String name() {
            return HTTP_2;
        }

        @Override
        public HttpHeaders newHeaders() {
            return new NettyHttp2Headers();
        }

        @Override
        public HttpHeaders newHeaders(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
            return new NettyHttp2Headers(headers);
        }
    }
}
