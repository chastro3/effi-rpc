package io.effi.rpc.registry.consul;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;

/**
 * Close vertx instance.
 */
@Extension("vertxCloser")
public class VertxCloser implements ScopedApplication.Listener {

    @Override
    public void onClosed(ScopedApplication application) {
        //todo close vertx instance
//        Object vertx = application.get(GenericKey.valueOf(KeyConstant.VERTX));
//        if (vertx != null) ((Vertx) vertx).close();
    }
}
