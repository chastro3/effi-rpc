package io.effi.rpc.transport.netty;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ExternalComponent;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.support.Scheduler;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.SystemKeys;
import io.effi.rpc.util.StringUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

@Extension("clientNioEventLoopGroupRegister")
public class ClientNioEventLoopGroupRegister implements ScopedPlatform.Listener {

    @Override
    public void onInitialized(ScopedPlatform platform) {
        NioEventLoopGroup nioEventLoopGroup = createNioEventLoopGroup();
        ExternalComponent<NioEventLoopGroup> component = new ExternalComponent<>(
                NettyClient.EVENT_LOOP_GROUP_KEY,
                nioEventLoopGroup,
                nioEventLoopGroup::shutdownGracefully);
        platform.registry().register(ExternalComponent.class, component);
        Scheduler scheduler = new Scheduler().withDisposableService(nioEventLoopGroup);
        platform.registry().register(Scheduler.class, scheduler);
    }

    private NioEventLoopGroup createNioEventLoopGroup() {
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
        return new NioEventLoopGroup(finalIoThreads, new DefaultThreadFactory("netty-client-worker", false));
    }
}
