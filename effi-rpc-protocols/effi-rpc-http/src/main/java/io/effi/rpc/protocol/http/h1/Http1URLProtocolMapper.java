package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.URLProtocolMapper;

import static io.effi.rpc.constant.Component.Protocol.HTTP_1_1;
import static io.effi.rpc.constant.Constant.HTTP;

/**
 * Supports {@link URLProtocolMapper} for HTTP/1.1 protocol.
 */

@Extension(HTTP_1_1)
public class Http1URLProtocolMapper implements URLProtocolMapper {
    @Override
    public String supported() {
        return HTTP_1_1;
    }

    @Override
    public String mapped() {
        return HTTP;
    }
}
