package io.effi.rpc.protocol.http;

import io.effi.rpc.option.OptionName;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectDecoder;
import static io.effi.rpc.option.OptionTypes.INTEGER;
import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines HTTP interaction options shared by callers and servants.
 */
public interface HttpOptions {

    OptionName<String> HTTP_METHOD = STRING.onlyCurrent("http.method", HttpMethod.GET.name());

    OptionName<Integer> DECODER_INITIAL_BUFFER_SIZE =
            INTEGER.onlyCurrent("http.decoderInitialBufferSize", HttpObjectDecoder.DEFAULT_INITIAL_BUFFER_SIZE);

    OptionName<Integer> MAX_MESSAGE_SIZE = INTEGER.onlyCurrent("http.maxMessageSize", 1024 * 32);
}
