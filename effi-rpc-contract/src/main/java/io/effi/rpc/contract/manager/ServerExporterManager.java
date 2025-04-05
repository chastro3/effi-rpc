package io.effi.rpc.contract.manager;

import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ServerExporter;

/**
 * Manage the registration and retrieval of {@link ServerExporter} instances.
 */
public class ServerExporterManager extends AbstractComponentManager<ServerExporter> {
    public ServerExporterManager(EffiRpcModule module) {
        super(module);
    }

    public Callee<?> getCallee(URL url) {
        ServerExporter serverExporter = get(url.authority());
        return serverExporter.calleeManager().get(url);
    }

}
