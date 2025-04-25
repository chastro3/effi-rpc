package io.effi.rpc.transport.netty;

import io.effi.rpc.spi.Extension;
import io.effi.rpc.contract.module.ApplicationConfiguration;
import io.effi.rpc.contract.module.EffRpcApplication;

/**
 * Close netty client event loop group.
 */
@Extension("clientEventGroupCloser")
public class ClientEventGroupCloser implements ApplicationConfiguration {

    @Override
    public void postStop(EffRpcApplication effRpcApplication) {
        NettyClient.closeNioEventLoopGroup();
    }
}
