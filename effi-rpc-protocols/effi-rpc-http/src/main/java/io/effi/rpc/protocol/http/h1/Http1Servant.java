package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.protocol.http.HttpServant;

/**
 * Implements {@link Servant} using http1.1.
 */
public class Http1Servant extends HttpServant {

    Http1Servant(Builder builder) {
        super(builder);
    }

    public static Builder builder(ServantMethod<?> servantMethod) {
        return new Builder(servantMethod);
    }

    /**
     * Builds {@link Http1Servant} instance.
     */
    public static class Builder extends HttpServant.Builder<Http1Servant, Builder> {

        public Builder(ServantMethod<?> servantMethod) {
            super(Http1Protocol.VERSION, servantMethod);
        }

        @Override
        public Http1Servant build() {
            return new Http1Servant(this);
        }
    }

}
