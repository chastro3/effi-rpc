package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.OptionName;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;
import static io.effi.rpc.config.OptionName.Strategy.CURRENT_FIRST;

public interface ConfigurablePeer extends Configurable, Peer {

    OptionName<String> THREAD_POOL_CONFIGURATOR = OptionName.of("threadPoolConfigurator",CURRENT_FIRST);

    OptionName<String> STAGE_CHAIN_CONFIGURATOR = OptionName.of("stageChainConfigurator", CURRENT_FIRST);

    OptionName<String> INTERCEPTOR_CHAIN_CONFIGURATOR = OptionName.of("interceptorChainConfigurator", CURRENT_FIRST);

    ConfigurablePeer threadPool(ThreadPool threadPool);

    ConfigurablePeer callStageChain(Stage.Chain chain);

    ConfigurablePeer replyStageChain(Stage.Chain chain);

    ConfigurablePeer callInterceptorChain(Interceptor.Chain chain);

    ConfigurablePeer replyInterceptorChain(Interceptor.Chain chain);

    @Extensible(scope = MODULE)
    interface ThreadPoolConfigurator extends Configurator<ConfigurablePeer> {}

    @Extensible(scope = MODULE)
    interface StageChainConfigurator extends Configurator<ConfigurablePeer> {}

    @Extensible(scope = MODULE)
    interface InterceptorChainConfigurator extends Configurator<ConfigurablePeer> {}

}
