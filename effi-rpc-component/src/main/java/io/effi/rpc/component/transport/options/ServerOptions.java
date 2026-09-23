package io.effi.rpc.component.transport.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;

/**
 * Defines server transport options.
 */
public interface ServerOptions {

    OptionName<Integer> ACCEPT_BACKLOG = INTEGER.onlyCurrent("transport.server.acceptBacklog", 1024);

    OptionName<Integer> ACCEPTOR_THREADS = INTEGER.onlyCurrent("transport.server.acceptorThreads", 1);

    OptionName<Integer> IO_THREADS = INTEGER.onlyCurrent("transport.server.ioThreads", Runtime.getRuntime().availableProcessors() * 2);
}
