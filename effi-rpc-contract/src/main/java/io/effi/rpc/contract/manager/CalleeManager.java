package io.effi.rpc.contract.manager;

import io.effi.rpc.common.config.URL;
import io.effi.rpc.common.config.URLUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.List;

/**
 * Manage the registration and retrieval of {@link Callee} instances.
 */
public class CalleeManager extends AbstractComponentManager<Callee<?>> {

    public CalleeManager(EffiRpcModule module) {
        super(module);
    }

    /**
     * Gets a callee by its request URL.
     *
     * @param url
     * @return
     * @see Invoker#managerKey()
     */
    public Callee<?> get(URL url) {
        return get(url.paths());
    }

    private Callee<?> get(List<String> paths) {
        return get(URLUtil.toPath(paths));
    }

}

