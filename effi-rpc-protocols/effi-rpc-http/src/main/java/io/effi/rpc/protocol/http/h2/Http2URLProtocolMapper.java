package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.URLProtocolMapper;
import io.effi.rpc.annotation.spi.Extension;

import static io.effi.rpc.constant.Constant.HTTP;
import static io.effi.rpc.constant.Component.Protocol.HTTP_2;

/**
 * Supports {@link URLProtocolMapper} for HTTP/2.0 protocol.
 */
@Extension(HTTP_2)
public class Http2URLProtocolMapper implements URLProtocolMapper {

    @Override
    public String supported() {
        return HTTP_2;
    }

    @Override
    public String mapped() {
        return HTTP;
    }
}