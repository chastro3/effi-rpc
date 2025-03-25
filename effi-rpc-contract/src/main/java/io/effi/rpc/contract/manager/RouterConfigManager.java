package io.effi.rpc.contract.manager;

import io.effi.rpc.contract.config.RouterConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import org.intellij.lang.annotations.Language;

/**
 * RouterConfig Manager.
 */
public class RouterConfigManager extends AbstractManager<RouterConfig> {
    public RouterConfigManager(EffiRpcModule module) {
        super(module);
    }

    /**
     * Register a new router config bye call url regex and target url regex.
     *
     * @param urlRegex
     * @param targetRegex
     */
    public void register(@Language("RegExp") String urlRegex, @Language("RegExp") String targetRegex) {
        RouterConfig routerConfig = new RouterConfig(urlRegex).match(targetRegex);
        register(urlRegex, routerConfig);
    }
}
