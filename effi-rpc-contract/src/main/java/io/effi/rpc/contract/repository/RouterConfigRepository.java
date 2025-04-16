package io.effi.rpc.contract.repository;

import io.effi.rpc.contract.config.RouterConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import org.intellij.lang.annotations.Language;

/**
 * RouterConfig Manager.
 */
public class RouterConfigRepository extends AbstractComponentRepository<RouterConfig> {
    public RouterConfigRepository(EffiRpcModule module) {
        super(module);
    }

    /**
     * Register a new router config bye call config regex and target config regex.
     *
     * @param urlRegex
     * @param targetRegex
     */
    public void register(@Language("RegExp") String urlRegex, @Language("RegExp") String targetRegex) {
        RouterConfig routerConfig = new RouterConfig(urlRegex).match(targetRegex);
        register(urlRegex, routerConfig);
    }
}
