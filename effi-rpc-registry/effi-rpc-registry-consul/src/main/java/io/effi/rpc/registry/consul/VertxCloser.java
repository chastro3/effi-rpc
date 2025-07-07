package io.effi.rpc.registry.consul;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ApplicationConfiguration;
import io.effi.rpc.component.EffiRpcApplication;

/**
 * Close vertx instance.
 */
@Extension("vertxCloser")
public class VertxCloser implements ApplicationConfiguration {

    @Override
    public void postStop(EffiRpcApplication application) {
        //todo close vertx instance
//        Object vertx = application.get(GenericKey.valueOf(KeyConstant.VERTX));
//        if (vertx != null) ((Vertx) vertx).close();
    }
}
