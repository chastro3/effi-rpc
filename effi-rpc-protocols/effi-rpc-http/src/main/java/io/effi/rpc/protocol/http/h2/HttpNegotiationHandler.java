package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

/**
 * Handles HTTP protocol negotiation and configures the channel
 * based on whether the client uses HTTP/2 or HTTP/1.1.
 */
@NativeConfig.Reflect(typeReached = Http2Protocol.class, queryAllPublicMethods = true)
public class HttpNegotiationHandler extends ApplicationProtocolNegotiationHandler {

    private final HttpCombineChannelConfigurer configurer;

    public HttpNegotiationHandler(HttpCombineChannelConfigurer configurer) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.configurer = AssertUtil.notNull(configurer, "configurer");
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        switch (protocol) {
            case ApplicationProtocolNames.HTTP_2:
                configurer.configureChannel(ctx.channel(), true);
                break;
            case ApplicationProtocolNames.HTTP_1_1:
                configurer.configureChannel(ctx.channel(), false);
                break;
            default:
                throw new IllegalStateException("Unknown protocol: " + protocol);
        }
    }
}