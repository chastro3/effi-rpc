package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.transport.ClientConfig;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

public interface ConfigurableCaller<R> extends ConfigurablePeer, Caller<R> {

    ConfigurableCaller<R> clientConfig(ClientConfig clientConfig);

    ConfigurableCaller<R> locator(Locator locator);

    ConfigurableCaller<R> chosenInterceptorChain(Interceptor.Chain chain);

    @Extensible(scope = MODULE)
    interface ClientConfigConfigurator extends Configurator<ConfigurableCaller<?>> {}

    @Extensible(scope = MODULE)
    interface LocatorConfigurator extends Configurator<ConfigurableCaller<?>> {}

}
