package io.effi.rpc.transport;

import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.common.spi.Extensible;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.common.util.resoruce.Cleanable;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.transport.codec.ClientCodec;
import io.effi.rpc.transport.codec.ServerCodec;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;

import java.util.Collection;

import static io.effi.rpc.common.constant.Component.Protocol.H2;

/**
 * Communication protocol within the system.
 */
@Extensible(H2)
public interface Protocol extends Cleanable {

    /**
     * Returns the protocol name.
     */
    String protocol();

    /**
     * Opens a client, reusing an existing instance if available.
     *
     * @param url    the client configuration URL
     * @param module the associated module
     * @return the client instance
     */
    Client openClient(URL url, EffiRpcModule module);

    /**
     * Opens a server based on the provided configuration.
     *
     * @param url    the server configuration URL
     * @param module the associated module
     * @return the server instance
     */
    Server openServer(URL url, EffiRpcModule module);

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
     * Creates a callee instance based on the provided method mapper and configuration.
     *
     * @param methodMapper the method mapping
     * @param config       the configuration
     * @param modules      optional modules
     * @return the created callee instance
     */
    <T> Callee<T> createCallee(MethodMapper<T> methodMapper, NodeConfig config, EffiRpcModule... modules);

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
     * Returns all active clients.
     */
    Collection<Client> clients();

    /**
     * Returns all active servers.
     */
    Collection<Server> servers();

    /**
     * Returns the server-side codec.
     */
    ServerCodec serverCodec();

    /**
     * Returns the client-side codec.
     */
    ClientCodec clientCodec();
}


