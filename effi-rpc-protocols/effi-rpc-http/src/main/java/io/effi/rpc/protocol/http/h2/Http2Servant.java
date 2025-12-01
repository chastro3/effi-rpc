package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.protocol.http.HttpServant;

/**
 * Implements {@link Servant} using http2.
 */
public class Http2Servant extends HttpServant {

    Http2Servant(Builder builder) {
        super(builder);
    }

    public static Builder builder(ServantMethod<?> servantMethod) {
        return new Builder(servantMethod);
    }


    /**
     * Builds {@link Http2Servant} instance.
     */
    public static class Builder extends HttpServant.Builder<Http2Servant, Builder> {

        public Builder(ServantMethod<?> servantMethod) {
            super(Http2Protocol.VERSION, servantMethod);
        }

        @Override
        public Http2Servant build() {
            return new Http2Servant(this);
        }

    }
}
