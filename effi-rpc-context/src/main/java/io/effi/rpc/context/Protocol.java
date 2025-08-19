package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.transport.ProtocolStack;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;
import static io.effi.rpc.config.ConfigValues.Protocol.HTTP_2;

/**
 * Defines RPC protocols with call side factory and message factory capabilities.
 * <p>
 * Provides a unified interface for protocol implementations that support
 * both call side creation and message factory functionality.
 */
@Extensible(value = HTTP_2, scope = PLATFORM)
public interface Protocol extends PeerFactory, MessageFactory {

    /**
     * Returns the name of this protocol.
     */
    String name();

    /**
     * Returns the protocol stack used by this protocol.
     */
    ProtocolStack stack();

    /**
     * Supplies access to the {@link Protocol}.
     */
    interface Supplier {

        /**
         * Returns the associated {@link Protocol}.
         */
        Protocol protocol();

    }

}
