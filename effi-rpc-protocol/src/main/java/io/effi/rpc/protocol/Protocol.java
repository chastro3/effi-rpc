package io.effi.rpc.protocol;

import io.effi.rpc.common.extension.resoruce.Cleanable;
import io.effi.rpc.common.extension.spi.Extensible;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.protocol.client.Client;
import io.effi.rpc.protocol.codec.ClientCodec;
import io.effi.rpc.protocol.codec.ServerCodec;
import io.effi.rpc.protocol.server.Server;

import java.util.Collection;

import static io.effi.rpc.common.constant.Component.Protocol.H2;

/**
 * Communication protocol within the system.
 */
@Extensible(H2)
public interface Protocol extends ProtocolAdapter, Cleanable {

    /**
     * Returns the name of this protocol.
     *
     * @return A {@link String} representing the name of the protocol.
     */
    String protocol();

    /**
     * Opens a client instance, potentially reusing an existing one if available.
     *
     * @param url    The core {@link ClientConfig} for the client to be opened.
     * @param module The {@link EffiRpcModule} associated with the client.
     * @return A {@link Client} instance associated with the specified configuration.
     */
    Client openClient(URL url, EffiRpcModule module);

    /**
     * Opens a new server instance based on the provided configuration.
     *
     * @param url    The core {@link ServerConfig} for the server to be opened.
     * @param module The {@link EffiRpcModule} associated with the client.
     * @return A {@link Server} instance associated with the specified configuration.
     */
    Server openServer(URL url, EffiRpcModule module);

    /**
     * Returns a collection of all currently open client connections.
     *
     * @return The collection of {@link Client} instances representing open connections.
     */
    Collection<Client> clients();

    /**
     * Returns a collection of all currently open server connections.
     *
     * @return The collection of {@link Server} instances representing open connections.
     */
    Collection<Server> servers();

    /**
     * Returns the server codec used for encoding and decoding messages on the server side.
     *
     * @return A {@link ServerCodec} instance for server-side message handling.
     */
    ServerCodec serverCodec();

    /**
     * Returns the client codec used for encoding and decoding messages on the client side.
     *
     * @return A {@link ClientCodec} instance for client-side message handling.
     */
    ClientCodec clientCodec();

}

