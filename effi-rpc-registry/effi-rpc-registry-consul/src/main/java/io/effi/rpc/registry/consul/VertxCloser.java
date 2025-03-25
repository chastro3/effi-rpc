package io.effi.rpc.registry.consul;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.extension.GenericKey;
import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.contract.module.ApplicationConfiguration;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.vertx.core.Vertx;

/**
 * Close vertx instance.
 */
@Extension("vertxCloser")
public class VertxCloser implements ApplicationConfiguration {

    @Override
    public void postStop(EffRpcApplication application) {
        Object vertx = application.get(GenericKey.valueOf(KeyConstant.VERTX));
        if (vertx != null) ((Vertx) vertx).close();
    }
}
