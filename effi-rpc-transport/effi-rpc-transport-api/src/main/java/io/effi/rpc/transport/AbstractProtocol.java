package io.effi.rpc.transport;

import io.effi.rpc.transport.codec.ClientCodec;
import io.effi.rpc.transport.codec.ServerCodec;
import io.effi.rpc.util.AssertUtil;

/**
 * Provides an abstract implementation of {@link Protocol}.
 */
public abstract class AbstractProtocol implements Protocol {

    protected String protocol;

    protected ServerCodec serverCodec;

    protected ClientCodec clientCodec;

    protected Transporter transporter;

    protected AbstractProtocol(String protocol, ServerCodec serverCodec, ClientCodec clientCodec) {
        this.protocol = AssertUtil.notBlank(protocol, "protocol");
        this.serverCodec = AssertUtil.notNull(serverCodec, "serverCodec");
        this.clientCodec = AssertUtil.notNull(clientCodec, "clientCodec");
    }

    public void transporter(Transporter transporter) {
        this.transporter = AssertUtil.notNull(transporter, "transporter");
    }

    @Override
    public ServerCodec serverCodec() {
        return serverCodec;
    }

    @Override
    public ClientCodec clientCodec() {
        return clientCodec;
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public Transporter transporter() {
        return transporter;
    }

    @Override
    public synchronized void clear() {
        transporter.clear();
    }
}
