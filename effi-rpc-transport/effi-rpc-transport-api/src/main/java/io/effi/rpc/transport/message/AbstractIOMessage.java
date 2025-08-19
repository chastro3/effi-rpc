package io.effi.rpc.transport.message;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.AssertUtil;

/**
 * Provides the default abstract implementation of {@link IOMessage}.
 */
public abstract class AbstractIOMessage implements IOMessage {

    protected SmartURL url;

    protected final Channel channel;

    protected AbstractIOMessage(SmartURL url, Channel channel) {
        this.url = AssertUtil.notNull(url, "url");
        this.channel = AssertUtil.notNull(channel, "channel");
    }

    @Override
    public SmartURL url() {
        return url;
    }

    @Override
    public Channel channel() {
        return channel;
    }

}

