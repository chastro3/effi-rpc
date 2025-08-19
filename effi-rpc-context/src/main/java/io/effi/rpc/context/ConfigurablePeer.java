package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.support.ThreadPool;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

public interface ConfigurablePeer extends Configurable, Peer {

    ConfigurablePeer withThreadPool(ThreadPool threadPool);

    ConfigurablePeer withCallStageChain(Stage.Chain chain);

    ConfigurablePeer withReplyStageChain(Stage.Chain chain);

    ConfigurablePeer withCallInterceptorChain(Interceptor.Chain chain);

    ConfigurablePeer withReplyInterceptorChain(Interceptor.Chain chain);

    @Extensible(scope = MODULE)
    interface ThreadPoolConfigurator extends Configurator<ConfigurablePeer> {}

    @Extensible(scope = MODULE)
    interface StageChainConfigurator extends Configurator<ConfigurablePeer> {}

    @Extensible(scope = MODULE)
    interface InterceptorChainConfigurator extends Configurator<ConfigurablePeer> {}

}
