package io.effi.rpc.protocol.http;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;
import io.effi.rpc.protocol.http.codec.HttpClientCodec;
import io.effi.rpc.protocol.http.codec.HttpServerCodec;
import io.effi.rpc.protocol.http.support.*;
import io.effi.rpc.transport.AbstractProtocol;
import io.effi.rpc.transport.Transporter;
import io.effi.rpc.transport.endpoint.Channel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Abstract Http implementation of {@link Protocol}.
 */
public abstract class HttpProtocol extends AbstractProtocol {

    private static final Map<CharSequence, CharSequence> REGULAR_REQUEST_HEADERS = HttpUtil.regularRequestHeaders();

    private static final Map<CharSequence, CharSequence> RESPONSE_REQUEST_HEADERS = HttpUtil.regularResponseHeaders();

    private final HttpVersion version;

    protected HttpProtocol(HttpVersion version, Transporter transporter) {
        super(version.protocolName(), transporter, new HttpServerCodec(), new HttpClientCodec());
        this.version = version;
    }

    @Override
    public Envelope.Request createRequest(Caller<?> caller, Object[] args) {
        if (caller instanceof HttpCaller<?> httpCaller) {
            HttpArgumentWrapper argumentWrapper = new HttpArgumentWrapper(caller, args);
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            headers.add(REGULAR_REQUEST_HEADERS);
            if (CollectionUtil.isNotEmpty(argumentWrapper.headers())) {
                argumentWrapper.headers().forEach(headers::add);
            }
            HttpUtil.setContentType(headers, caller.config());
            return HttpRequest.builder()
                    .version(version)
                    .method(httpCaller.httpMethod())
                    .url(argumentWrapper.requestUrl())
                    .headers(headers)
                    .body(argumentWrapper.body())
                    .build();
        }
        throw new IllegalArgumentException(Messages.unSupport("caller", caller.getClass()));
    }


    @Override
    public Envelope.Response createResponse(Callee<?> callee, Result result) {
        if (callee instanceof HttpCallee<?> httpCallee) {
            int statusCode = 200;
            Object value = result.value();
            if (result.hasException()) {
                value = result.exception().getMessage();
                statusCode = 500;
            }
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            headers.add(RESPONSE_REQUEST_HEADERS);
            HttpUtil.setContentType(headers, callee.config());
            return HttpResponse.builder()
                    .version(version)
                    .method(httpCallee.httpMethod())
                    .statusCode(statusCode)
                    .url(result.url())
                    .headers(headers)
                    .body(value)
                    .build();
        }
        throw new IllegalArgumentException("unsupported callee type :" + ObjectUtil.simpleClassName(callee));
    }

    @Override
    public void sendCalleeNotFound(Envelope.Request request, Channel channel) {
        HttpResponse<byte[]> httpResponse = create404Response(request, channel);
        channel.send(httpResponse);
    }

    private HttpResponse<byte[]> create404Response(Envelope.Request request, Channel channel) {
        EffiRpcException ex = PredefinedErrorCode.NOT_FOUND_CALLEE.fail(null, request.url().uri());
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(RESPONSE_REQUEST_HEADERS);
        headers.add(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        return HttpResponse.builder()
                .version(version)
                .method(HttpMethod.GET)
                .statusCode(404)
                .url(request.url())
                .headers(headers)
                .body(ex.getMessage().getBytes())
                .build();
    }

    @Override
    public Class<? extends Envelope.Request> supportedRequestType() {
        return HttpRequest.class;
    }

    @Override
    public Class<? extends Envelope.Response> supportedResponseType() {
        return HttpResponse.class;
    }

    public HttpVersion version() {
        return version;
    }

}
