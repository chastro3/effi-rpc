package io.effi.rpc.transport;

import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.contract.module.ApplicationConfiguration;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.transport.client.NettyClient;

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
