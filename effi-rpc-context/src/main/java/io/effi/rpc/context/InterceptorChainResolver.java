package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedModule;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Resolves the interceptor chains for a peer.
 */
@Extensible(scope = MODULE)
public interface InterceptorChainResolver {

    Interceptor.Chain resolveCallChain(PeerDescriptor descriptor, ScopedModule module);

    Interceptor.Chain resolveChosenChain(PeerDescriptor descriptor, ScopedModule module);

    Interceptor.Chain resolveReplyChain(PeerDescriptor descriptor, ScopedModule module);
}
