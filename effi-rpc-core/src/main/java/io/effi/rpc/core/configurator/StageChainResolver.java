package io.effi.rpc.core.configurator;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.core.PeerDescriptor;
import io.effi.rpc.context.Stage;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Resolves the call and reply stage chains for a peer.
 */
@Extensible(scope = MODULE)
public interface StageChainResolver {

    Stage.Chain resolveCallChain(PeerDescriptor descriptor, ScopedModule module);

    Stage.Chain resolveReplyChain(PeerDescriptor descriptor, ScopedModule module);
}
