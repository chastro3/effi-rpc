package io.effi.rpc.transport.netty;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ExternalComponent;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.SystemKeys;
import io.effi.rpc.util.StringUtil;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * Registers the shared client event loop group.
 */
@Extension("clientEventLoopGroupRegister")
public class ClientEventLoopGroupRegister implements ScopedPlatform.Listener {

    @Override
    public void onInitialized(ScopedPlatform platform) {
        MultiThreadIoEventLoopGroup eventLoopGroup = createEventLoopGroup();
        ExternalComponent<MultiThreadIoEventLoopGroup> component = new ExternalComponent<>(
                NettyClient.EVENT_LOOP_GROUP_KEY,
                eventLoopGroup,
                eventLoopGroup::shutdownGracefully);
        platform.registry().register(ExternalComponent.class, component);
    }

    private MultiThreadIoEventLoopGroup createEventLoopGroup() {
        // todo 使用 platform config
        String ioThreadsStr = System.getProperty(SystemKeys.CLIENT_IO_THREADS);
        int ioThreads = Constant.DEFAULT_IO_THREADS;
        if (StringUtil.isNotBlank(ioThreadsStr)) {
            try {
                ioThreads = Math.max(Integer.parseInt(ioThreadsStr), ioThreads);
            } catch (NumberFormatException ignore) {
            }
        }
        int finalIoThreads = ioThreads;
        return new MultiThreadIoEventLoopGroup(
                finalIoThreads,
                new DefaultThreadFactory("netty-client-worker", false),
                NioIoHandler.newFactory()
        );
    }
}
