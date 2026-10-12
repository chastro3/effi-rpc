package io.effi.rpc.protocol.grpc.server;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Servant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.protocol.grpc.GrpcProtocol;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.stream.GrpcServerStream;
import io.effi.rpc.transport.TransportErrorCodes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;

/**
 * Provides gRPC call resolution and stream handoff to {@link GrpcServerHandler}.
 */
public final class GrpcCallResolver extends ChannelInboundHandlerAdapter {

    private final GrpcServer server;

    public GrpcCallResolver(GrpcServer server) {
        this.server = server;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Http2HeadersFrame headersFrame)) {
            super.channelRead(ctx, msg);
            return;
        }
        CharSequence path = headersFrame.headers().path();
        if (path == null) {
            fail(ctx, TransportErrorCodes.DECODE.fail(GrpcProtocol.NAME, ":path"));
            return;
        }
        Servant servant = lookup(headersFrame.headers(), path);
        if (!(servant instanceof GrpcServant grpcServant)) {
            fail(ctx, InteractionErrorCodes.SERVANT_NOT_FOUND.fail(path));
            return;
        }
        ctx.pipeline().replace(this, "grpcServerHandler", new GrpcServerHandler(
                server,
                grpcServant,
                GrpcSupport.serializer(servant),
                ctx.channel(),
                path.toString()
        ));
    }

    private Servant lookup(Http2Headers headers, CharSequence path) {
        ScopedModule module = lookupModule(headers);
        return module == null
                ? null
                : module.namedComponent(Servant.class, Peer.buildId(GrpcProtocol.NAME, path.toString()));
    }

    private ScopedModule lookupModule(Http2Headers headers) {
        ScopedPlatform platform = server.platform();
        if (platform.applications().size() == 1) {
            ScopedApplication application = platform.applications().iterator().next();
            if (application.modules().size() == 1) {
                return application.modules().iterator().next();
            }
        }
        CharSequence applicationName = headers.get(KeyConstant.REQUEST_REMOTE_APPLICATION);
        CharSequence moduleName = headers.get(KeyConstant.REQUEST_REMOTE_MODULE);
        ScopedApplication application = platform.lookupApplication(
                applicationName == null ? Constant.DEFAULT_NAME : applicationName.toString()
        );
        return application == null
                ? null
                : application.lookupModule(moduleName == null ? Constant.DEFAULT_NAME : moduleName.toString());
    }

    private void fail(ChannelHandlerContext ctx, EffiRpcException cause) {
        GrpcServerStream stream = new GrpcServerStream(ctx.channel());
        stream.start();
        stream.fail(cause);
    }
}
