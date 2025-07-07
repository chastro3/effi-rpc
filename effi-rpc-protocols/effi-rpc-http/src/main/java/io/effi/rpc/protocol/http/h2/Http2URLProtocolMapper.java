package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.URLProtocolMapper;

import static io.effi.rpc.constant.Component.Protocol.HTTP_2;
import static io.effi.rpc.constant.Constant.HTTP;

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