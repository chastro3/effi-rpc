package io.effi.rpc.boot.confiurator;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.PeerDescriptor;
import io.effi.rpc.context.ThreadPoolResolver;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.boot.confiurator.DefaultThreadPoolResolver.NAME;
import static io.effi.rpc.context.options.ThreadPoolOptions.THREAD_POOL;

/**
 * Resolves the default thread pool for a peer.
 */
@Extension(value = NAME, primary = true)
public class DefaultThreadPoolResolver implements ThreadPoolResolver {

    public static final String NAME = Constant.DEFAULT_NAME;

    private static final String CALLER_HYBRID_NAME = "caller-hybrid";

    private static final String SERVANT_HYBRID_NAME = "servant-hybrid";

    private final LazySingleton<ThreadPool> callerHybridThreadPool = LazySingleton.from(() ->
            new ThreadPool(CALLER_HYBRID_NAME, RpcThreadPool.defaultCPUExecutor(CALLER_HYBRID_NAME)));

    private final LazySingleton<ThreadPool> servantHybridThreadPool = LazySingleton.from(() ->
            new ThreadPool(SERVANT_HYBRID_NAME, RpcThreadPool.defaultIOExecutor(SERVANT_HYBRID_NAME)));

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
                ? callerHybridThreadPool.ensure()
                : servantHybridThreadPool.ensure();
    }
}
