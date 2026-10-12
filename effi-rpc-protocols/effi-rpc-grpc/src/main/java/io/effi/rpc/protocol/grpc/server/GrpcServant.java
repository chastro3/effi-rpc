package io.effi.rpc.protocol.grpc.server;

import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.protocol.grpc.GrpcMethod;
import io.effi.rpc.protocol.grpc.GrpcProtocol;
import io.effi.rpc.protocol.http.HttpServant;
import io.effi.rpc.serialization.protobuf.ProtobufSerializer;
import io.effi.rpc.util.StringUtil;

/**
 * Provides the gRPC servant.
 */
public class GrpcServant extends HttpServant {

    private final GrpcMethod grpcMethod;

    GrpcServant(Builder builder) {
        super(builder);
        this.grpcMethod = new GrpcMethod(method());
    }

    public static Builder builder(ServantMethod<?> servantMethod) {
        return new Builder(servantMethod);
    }

    /**
     * Returns the static contract of this gRPC method.
     */
    public GrpcMethod grpcMethod() {
        return grpcMethod;
    }

    /**
     * Builds {@link GrpcServant} instances.
     */
    public static class Builder extends HttpServant.Builder<GrpcServant, Builder> {

        public Builder(ServantMethod<?> servantMethod) {
            super(GrpcProtocol.VERSION, servantMethod);
        }

        @Override
        protected GrpcServant newInstance() {
            return new GrpcServant(this);
        }

        @Override
        protected void validate() {
            if (StringUtil.isBlank(option(SerializationOptions.SERIALIZER))) {
                serializer(ProtobufSerializer.NAME);
            }
            super.validate();
        }
    }
}
