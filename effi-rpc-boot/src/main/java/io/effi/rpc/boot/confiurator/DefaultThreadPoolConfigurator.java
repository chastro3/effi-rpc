package io.effi.rpc.boot.confiurator;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.ConfigValues;
import io.effi.rpc.context.ConfigurablePeer;
import io.effi.rpc.context.ConfigurableCallee;
import io.effi.rpc.context.ConfigurableCaller;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.boot.confiurator.DefaultThreadPoolConfigurator.NAME;

@Extension(value = NAME, primary = true)
public class DefaultThreadPoolConfigurator implements ConfigurablePeer.ThreadPoolConfigurator {

    public static final String NAME = ConfigValues.DEFAULT;

    private static final String CALLER_HYBRID_NAME = "caller-hybrid";

    private static final String CALLEE_HYBRID_NAME = "callee-hybrid";

    private final LazySingleton<ThreadPool> callerHybridThreadPool = LazySingleton.from(() ->
            new ThreadPool(CALLER_HYBRID_NAME, RpcThreadPool.defaultCPUExecutor(CALLER_HYBRID_NAME)));

    private final LazySingleton<ThreadPool> calleeHybridThreadPool = LazySingleton.from(() ->
            new ThreadPool(CALLEE_HYBRID_NAME, RpcThreadPool.defaultIOExecutor(CALLEE_HYBRID_NAME)));

    @Override
    public void configure(ConfigurablePeer peer) {
        String configuredName = peer.getConfig(ConfigNames.THREAD_POOL);
        if (StringUtil.isBlank(configuredName)) {
            configureDefault(peer);
            return;
        }
        ThreadPool threadPool = peer.platform().namedComponent(ThreadPool.class, configuredName);
        if (threadPool != null) {
            peer.withThreadPool(threadPool);
        } else {
            configureDefault(peer);
        }
    }

    private void configureDefault(ConfigurablePeer peer) {
        if (peer instanceof ConfigurableCaller<?> caller) {
            caller.withThreadPool(callerHybridThreadPool.ensure());
        } else if (peer instanceof ConfigurableCallee callee) {
            callee.withThreadPool(calleeHybridThreadPool.ensure());
        }
    }
}
