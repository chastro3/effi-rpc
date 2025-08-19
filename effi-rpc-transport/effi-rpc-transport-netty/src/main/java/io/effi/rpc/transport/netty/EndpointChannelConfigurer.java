package io.effi.rpc.transport.netty;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.effi.rpc.util.AssertUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

/**
 * Provides a base implementation of {@link EndpointChannelConfigurer} for an endpoint.
 * <p>
 * Invokes {@link  #initChannel(Channel, EndpointConfig, boolean)} at the appropriate time
 * to initialize the {@link NettyChannel}.
 */
public abstract class EndpointChannelConfigurer<E extends Endpoint> implements ChannelConfigurer {

    protected E endpoint;

    protected SslContext sslContext;

    protected IdleDetectionHandler idleDetectionHandler;

    protected EndpointChannelConfigurer(E endpoint, SslContext sslContext) {
        this.endpoint = AssertUtil.notNull(endpoint, "endpoint");
        this.sslContext = sslContext;
        this.idleDetectionHandler = new IdleDetectionHandler(endpoint);
    }

    @Override
    public void configure(Channel channel, EndpointConfig config) {
        initChannel(channel, config, false);
        ChannelPipeline pipeline = channel.pipeline();
        configureSslHandlerIfAbsent(pipeline);
        configureIdleDetectionHandlerIfAbsent(pipeline);
        doConfigure(channel, config);
    }

    protected void initChannel(Channel channel, EndpointConfig config, boolean isVirtual) {
        NettyChannel.init(channel, endpoint, config, isVirtual);
    }

    protected void configureSslHandlerIfAbsent(ChannelPipeline pipeline) {
        if (sslContext != null && pipeline.get(SslHandler.class) == null) {
            pipeline.addLast("sslHandler", sslContext.newHandler(pipeline.channel().alloc()));
        }
    }

    protected void configureIdleDetectionHandlerIfAbsent(ChannelPipeline pipeline) {
        if (idleDetectionHandler != null && pipeline.get(IdleDetectionHandler.class) == null) {
            pipeline.addLast("idleDetectionHandler", idleDetectionHandler);
        }
    }

    protected void doConfigure(Channel channel, EndpointConfig config) {

    }
}



