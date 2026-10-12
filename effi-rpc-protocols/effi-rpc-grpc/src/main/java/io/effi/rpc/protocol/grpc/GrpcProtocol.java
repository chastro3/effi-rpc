package io.effi.rpc.protocol.grpc;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.protocol.grpc.client.GrpcCaller;
import io.effi.rpc.protocol.grpc.client.GrpcClient;
import io.effi.rpc.protocol.grpc.server.GrpcServant;
import io.effi.rpc.protocol.grpc.server.GrpcServer;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.protocol.http.HttpVersion;
import io.effi.rpc.protocol.http.h2.Http2Protocol;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.TypeCapture;

import java.net.InetSocketAddress;

import static io.effi.rpc.protocol.grpc.GrpcProtocol.NAME;

/**
 * Provides the gRPC transport protocol.
 */
@Extension(value = NAME, primary = false)
public class GrpcProtocol extends Http2Protocol {

    public static final String NAME = "grpc";

    public static final HttpVersion VERSION = new Version();

    public GrpcProtocol() {
        super(VERSION);
    }

    @Override
    public <T> Servant createServant(ServantMethod<T> servantMethod, HierarchicalOptions options, ScopedModule module) {
        return GrpcServant.builder(servantMethod).options(options).module(module).build();
    }

    @Override
    public <T> Caller<T> createCaller(TypeCapture<T> returnType, HierarchicalOptions options, ScopedModule module) {
        return GrpcCaller.builder(returnType).options(options).module(module).build();
    }

    @Override
    protected Server createServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
        return new GrpcServer(config, address, platform);
    }

    @Override
    protected Client createClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform) {
        return new GrpcClient(config, remoteAddress, platform);
    }

    private static final class Version extends Http2Protocol.Version {

        @Override
        public String name() {
            return NAME;
        }
    }
}
