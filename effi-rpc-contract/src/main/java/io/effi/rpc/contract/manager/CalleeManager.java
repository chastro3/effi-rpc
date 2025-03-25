package io.effi.rpc.contract.manager;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.url.URLUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.List;

/**
 * Manage the registration and retrieval of {@link Callee} instances.
 */
public class CalleeManager extends AbstractManager<Callee<?>> {

    public CalleeManager(EffiRpcModule module) {
        super(module);
    }

    /**
     * Acquires a callee by its request URL.
     *
     * @param url
     * @return
     * @see Invoker#managerKey()
     */
    public Callee<?> acquire(URL url) {
        return acquire(url.paths());
    }

    private Callee<?> acquire(List<String> paths) {
        return get(URLUtil.toPath(paths));
    }

}

