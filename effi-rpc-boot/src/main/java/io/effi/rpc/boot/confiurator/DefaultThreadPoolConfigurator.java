package io.effi.rpc.boot.confiurator;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.ConfigurableCaller;
import io.effi.rpc.context.ConfigurablePeer;
import io.effi.rpc.context.ConfigurableServant;
import io.effi.rpc.executor.RpcThreadPool;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.boot.confiurator.DefaultThreadPoolConfigurator.NAME;
import static io.effi.rpc.config.OptionName.Strategy.CURRENT_FIRST;

@Extension(value = NAME, primary = true)
public class DefaultThreadPoolConfigurator implements ConfigurablePeer.ThreadPoolConfigurator {

    public static final String NAME = Constant.DEFAULT_NAME;

    public static final OptionName<String> THREAD_POOL = ConfigurableOptionName.nameOf("threadPool", CURRENT_FIRST);

    private static final String CALLER_HYBRID_NAME = "caller-hybrid";

    private static final String SERVANT_HYBRID_NAME = "servant-hybrid";

    private final LazySingleton<ThreadPool> callerHybridThreadPool = LazySingleton.from(() ->
            new ThreadPool(CALLER_HYBRID_NAME, RpcThreadPool.defaultCPUExecutor(CALLER_HYBRID_NAME)));

    private final LazySingleton<ThreadPool> servantHybridThreadPool = LazySingleton.from(() ->
            new ThreadPool(SERVANT_HYBRID_NAME, RpcThreadPool.defaultIOExecutor(SERVANT_HYBRID_NAME)));

    @Override
    public void configure(ConfigurablePeer peer) {
        String configuredName = peer.option(THREAD_POOL);
        if (StringUtil.isBlank(configuredName)) {
            configureDefault(peer);
            return;
        }
        ThreadPool threadPool = peer.platform().namedComponent(ThreadPool.class, configuredName);
        if (threadPool != null) {
            peer.threadPool(threadPool);
        } else {
            configureDefault(peer);
        }
    }

    private void configureDefault(ConfigurablePeer peer) {
        if (peer instanceof ConfigurableCaller<?> caller) {
            caller.threadPool(callerHybridThreadPool.ensure());
        } else if (peer instanceof ConfigurableServant servant) {
            servant.threadPool(servantHybridThreadPool.ensure());
        }
    }
}
