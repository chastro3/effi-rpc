package io.effi.rpc.contract.repository;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.ServerExporter;

/**
 * Manages the registration and retrieval of {@link ServerExporter} instances.
 */
public class ServerExporterRepository extends AbstractComponentRepository<ServerExporter> {
    public ServerExporterRepository(EffiRpcModule module) {
        super(module);
    }

    public Callee<?> getCallee(URL url) {
        ServerExporter serverExporter = get(url.authority());
        return serverExporter.calleeRepository().get(url);
    }

}
