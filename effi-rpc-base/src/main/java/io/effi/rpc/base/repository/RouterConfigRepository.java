package io.effi.rpc.base.repository;

import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.MultiComponent;
import org.intellij.lang.annotations.Language;

/**
 * RouterConfig Manager.
 */
public class RouterConfigRepository extends MultiComponent<Object> {
    public RouterConfigRepository(EffiRpcModule module) {
    }

    /**
     * Register a new router config bye call config regex and target config regex.
     *
     * @param urlRegex
     * @param targetRegex
     */
    public void register(@Language("RegExp") String urlRegex, @Language("RegExp") String targetRegex) {

    }
}
