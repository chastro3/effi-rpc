package io.effi.rpc.boot.configurator;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.tools.ThreadPool;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.PeerDescriptor;
import io.effi.rpc.context.ThreadPoolResolver;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.boot.configurator.DefaultThreadPoolResolver.NAME;
import static io.effi.rpc.context.options.ThreadPoolOptions.THREAD_POOL;

/**
 * Provides default thread pool resolution for a peer.
 */
@Extension(value = NAME, primary = true)
public class DefaultThreadPoolResolver implements ThreadPoolResolver {

    public static final String NAME = Constant.DEFAULT_NAME;

    private static final String CALLER_HYBRID_NAME = "caller-hybrid";

    private static final String SERVANT_HYBRID_NAME = "servant-hybrid";

    @Override
    public ThreadPool resolve(PeerDescriptor descriptor, ScopedModule module) {
        String configuredName = descriptor.options().option(THREAD_POOL);
        if (StringUtil.isNotBlank(configuredName)) {
            ThreadPool threadPool = module.platform().namedComponent(ThreadPool.class, configuredName);
            if (threadPool != null) {
                return threadPool;
            }
        }
        return descriptor.kind() == PeerDescriptor.Kind.CALLER
                ? resolveDefault(module.platform(), CALLER_HYBRID_NAME, false)
                : resolveDefault(module.platform(), SERVANT_HYBRID_NAME, true);
    }

    private ThreadPool resolveDefault(ScopedPlatform platform, String name, boolean io) {
        synchronized (platform) {
            ThreadPool existing = platform.namedComponent(ThreadPool.class, name);
            if (existing != null) {
                return existing;
            }
            ThreadPool threadPool = new ThreadPool(
                    name,
                    io ? RpcThreadPool.defaultIOExecutor(name) : RpcThreadPool.defaultCPUExecutor(name)
            );
            platform.registry().register(ThreadPool.class, name, threadPool);
            return threadPool;
        }
    }
}
