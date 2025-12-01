package io.effi.rpc.transport;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Protocol;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.codec.ServerExchangeContextCodec;
import io.effi.rpc.transport.message.InputMessage;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Define protocols for client-server communication and request handling.
 * <p>
 * Provides transport protocol functionality for module lookup, error handling,
 * and codec management with platform-scoped extensibility.
 */
@Extensible(scope = PLATFORM)
public interface TransportProtocol extends Transporter, Protocol {

    /**
     * Looks up a module from the input message.
     *
     * @param inputMessage the input message
     */
    ScopedModule lookupModule(InputMessage inputMessage);

    /**
     * Sends a callee not found response to the specified channel.
     *
     * @param request the request
     * @param channel the channel
     */
    void sendCalleeNotFound(InputMessage inputMessage);

    /**
     * Returns the server-side codec.
     */
    ServerExchangeContextCodec serverCodec();

    /**
     * Returns the client-side codec.
     */
    ClientExchangeContextCodec clientCodec();

    /**
     * Supplies access to the {@link TransportProtocol}.
     */
    interface Supplier extends Protocol.Supplier{

        /**
         * Returns the associated {@link TransportProtocol}.
         */
        @Override
        TransportProtocol protocol();

    }
}


