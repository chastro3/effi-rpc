package io.effi.rpc.transport;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Protocol;
import io.effi.rpc.context.Response;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.codec.ServerExchangeContextCodec;
import io.effi.rpc.transport.message.InputMessage;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines the transport protocol contract for endpoints, codecs, and request handling.
 */
@Extensible(scope = PLATFORM)
public interface TransportProtocol extends Transporter, Protocol {

    /**
     * Looks up the module targeted by the input message.
     *
     * @param inputMessage the input message
     * @return the target module, or {@code null} when absent
     */
    ScopedModule lookupModule(InputMessage inputMessage);

    /**
     * Creates a protocol-specific response for a failed request.
     *
     * @param inputMessage the request
     * @param cause        the failure cause
     * @return the error response
     */
    Response createErrorResponse(InputMessage inputMessage, EffiRpcException cause);

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
    interface Supplier extends Protocol.Supplier {

        /**
         * Returns the associated {@link TransportProtocol}.
         */
        @Override
        TransportProtocol protocol();

    }
}


