package io.effi.rpc.transport;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Result;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.annotation.spi.Extensible;
import io.effi.rpc.transport.codec.ClientCodec;
import io.effi.rpc.transport.codec.ServerCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.TypeToken;
import io.effi.rpc.util.resoruce.Cleanable;

import static io.effi.rpc.constant.Component.Protocol.HTTP_2;

/**
 * Defines protocols for client-server communication and request handling.
 */
@Extensible(HTTP_2)
public interface Protocol extends Cleanable {

    /**
     * Returns the protocol name.
     */
    String protocol();

    /**
     * Returns the transporter.
     */
    Transporter transporter();

    /**
     * Creates a request for the given caller and arguments.
     *
     * @param caller the request initiator
     * @param args   the request arguments
     * @return the request envelope
     */
    Envelope.Request createRequest(Caller<?> caller, Object[] args);

    /**
     * Creates a response for the given callee and result.
     *
     * @param callee the response handler
     * @param result the response result
     * @return the response envelope
     */
    Envelope.Response createResponse(Callee<?> callee, Result result);

    /**
     * Sends a callee not found response to the specified channel.
     *
     * @param request the request
     * @param channel the channel
     */
    void sendCalleeNotFound(Envelope.Request request, Channel channel);

    /**
     * Creates a callee instance based on the provided method mapper and configuration.
     *
     * @param methodMapper the method mapping
     * @param config       the configuration
     * @param module      optional module
     * @return the created callee instance
     */
    <T> Callee<T> createCallee(MethodMapper<T> methodMapper, NodeConfig config, EffiRpcModule module);

    /**
     * Creates a caller instance with the specified return type and configuration.
     *
     * @param returnType the expected return type
     * @param config     the configuration
     * @param module     the associated module
     * @return the created caller instance
     */
    <T> Caller<T> createCaller(TypeToken<T> returnType, NodeConfig config, EffiRpcModule module);

    /**
     * Returns the supported request type.
     */
    Class<? extends Envelope.Request> supportedRequestType();

    /**
     * Returns the supported response type.
     */
    Class<? extends Envelope.Response> supportedResponseType();

    /**
     * Returns the server-side codec.
     */
    ServerCodec serverCodec();

    /**
     * Returns the client-side codec.
     */
    ClientCodec clientCodec();
}


