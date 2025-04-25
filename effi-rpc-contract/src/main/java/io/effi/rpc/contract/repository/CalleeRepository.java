package io.effi.rpc.contract.repository;

import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.module.EffiRpcModule;

import java.util.List;

/**
 * Manage the registration and retrieval of {@link Callee} instances.
 */
public class CalleeRepository extends AbstractComponentRepository<Callee<?>> {

    public CalleeRepository(EffiRpcModule module) {
        super(module);
    }

    /**
     * Gets a callee by its request URL.
     *
     * @param url
     * @return
     * @see Invoker#repositoryKey()
     */
    public Callee<?> get(URL url) {
        return get(url.paths());
    }

    private Callee<?> get(List<String> paths) {
        return get(URLUtil.toPath(paths));
    }

}

