package io.effi.rpc.transport;

import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.codec.ServerExchangeContextCodec;
import io.effi.rpc.util.AssertUtil;

/**
 * Provides an abstract implementation of {@link TransportProtocol}.
 */
public abstract class AbstractProtocol extends AbstractTransporter implements TransportProtocol {

    protected String protocolName;

    protected ProtocolStack stack;

    protected ServerExchangeContextCodec serverCodec;

    protected ClientExchangeContextCodec clientCodec;

    protected void initialize(String protocolName, ProtocolStack stack,
                              ServerExchangeContextCodec serverCodec,
                              ClientExchangeContextCodec clientCodec) {
        this.protocolName = AssertUtil.notBlank(protocolName, "protocol id");
        this.stack = AssertUtil.notNull(stack, "stack");
        this.serverCodec = AssertUtil.notNull(serverCodec, "serverCodec");
        this.clientCodec = AssertUtil.notNull(clientCodec, "clientCodec");
    }

    @Override
    public String name() {
        return protocolName;
    }

    @Override
    public ProtocolStack stack() {
        return stack;
    }

    @Override
    public ServerExchangeContextCodec serverCodec() {
        return serverCodec;
    }

    @Override
    public ClientExchangeContextCodec clientCodec() {
        return clientCodec;
    }
}
