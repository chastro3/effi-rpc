package io.effi.rpc.core.configurator;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.tools.ThreadPool;
import io.effi.rpc.core.PeerDescriptor;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Resolves the thread pool used by a peer.
 */
@Extensible(scope = MODULE)
public interface ThreadPoolResolver {

    ThreadPool resolve(PeerDescriptor descriptor, ScopedModule module);
}
