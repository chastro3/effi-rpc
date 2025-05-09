package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.netty.ChannelManageHandler;
import io.effi.rpc.transport.netty.NettyEndpointConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

/**
 * Handles HTTP protocol negotiation and configures the channel
 * based on whether the client uses HTTP/2 or HTTP/1.1.
 */
public class HttpNegotiationHandler extends ApplicationProtocolNegotiationHandler {

    private final HttpServerConfigurer configurer;

    protected HttpNegotiationHandler(ChannelManageHandler channelManager, NettyEndpointConfig h2config) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.configurer = new HttpServerConfigurer(channelManager, h2config);
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        switch (protocol) {
            case ApplicationProtocolNames.HTTP_2:
                System.out.println("-------------h2");
                configurer.configure(ctx.channel(), true);
                break;
            case ApplicationProtocolNames.HTTP_1_1:
                System.out.println("-------------h1");
                configurer.configure(ctx.channel(), false);
                break;
            default:
                throw new IllegalStateException("Unknown protocol: " + protocol);
        }
    }
}