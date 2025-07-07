package io.effi.rpc.transport.netty;


import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.component.PlatformConfiguration;
import io.effi.rpc.component.WrappedComponent;
import io.effi.rpc.config.SystemConfig;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.SystemKeys;
import io.effi.rpc.util.StringUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

@Extension("clientNioEventLoopGroupRegister")
public class ClientNioEventLoopGroupRegister implements PlatformConfiguration {

    @Override
    public void postInit(EffiRpcPlatform platform) {
        NioEventLoopGroup nioEventLoopGroup = createNioEventLoopGroup();
        WrappedComponent<NioEventLoopGroup> component = new WrappedComponent<>(
                NettyClient.EVENT_LOOP_GROUP_KEY,
                nioEventLoopGroup,
                nioEventLoopGroup::shutdownGracefully);
        platform.register(WrappedComponent.class, component);
    }

    private NioEventLoopGroup createNioEventLoopGroup() {
        String ioThreadsStr = SystemConfig.getInstance().getParam(SystemKeys.CLIENT_IO_THREADS);
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
