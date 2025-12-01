package io.effi.rpc.protocol.http;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Servant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.protocol.http.codec.HttpClientCodec;
import io.effi.rpc.protocol.http.codec.HttpServerCodec;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.AbstractProtocol;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.codec.ConfigurableClientCodec;
import io.effi.rpc.transport.codec.ConfigurableServerCodec;
import io.effi.rpc.transport.codec.ServerExchangeContextCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.ObjectUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Provides a standard http implementation of {@link TransportProtocol}.
 */
public abstract class HttpProtocol extends AbstractProtocol {

    public static final OptionName<String> HTTP_METHOD = ConfigurableOptionName.<String>nameOf("httpMethod").defaultValue(HttpMethod.GET.name());

    private static final Map<CharSequence, CharSequence> REGULAR_REQUEST_HEADERS = HttpUtil.regularRequestHeaders();

    private static final Map<CharSequence, CharSequence> RESPONSE_REQUEST_HEADERS = HttpUtil.regularResponseHeaders();

    private final HttpVersion version;

    protected HttpProtocol(HttpVersion version) {
        this.version = AssertUtil.notNull(version, "version");
        initialize(version().name(), ProtocolStack.TCP, createServerCodec(), createClientCodec());
    }

    @Override
    public Request createRequest(Caller<?> caller, Object[] args) {
        if (caller instanceof HttpCaller<?> httpCaller) {
            HttpInvocation argumentWrapper = new HttpInvocation(caller, args);
            HttpHeaders headers = version().newHeaders();
            headers.add(REGULAR_REQUEST_HEADERS.entrySet());
            Map<String, String> argumentHeaders = argumentWrapper.headers();
            if (CollectionUtil.isNotEmpty(argumentHeaders)) {
                headers.add(argumentHeaders.entrySet());
            }
            HttpUtil.addContentType(headers, caller.options());
            return HttpDuplexRequest.builder()
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
    public Response createResponse(Servant servant, Interaction.Result result) {
        if (servant instanceof HttpServant httpCallee) {
            int statusCode = 200;
            Object value = result.result();
            if (!result.succeeded()) {
                value = result.cause().getMessage();
                statusCode = 500;
            }
            HttpHeaders headers = version().newHeaders();
            headers.add(RESPONSE_REQUEST_HEADERS.entrySet());
            HttpUtil.addContentType(headers, servant.options());
            return HttpDuplexResponse.builder()
                    .version(version)
                    .method(httpCallee.httpMethod())
                    .statusCode(statusCode)
                    .url(result.url())
                    .headers(headers)
                    .body(value)
                    .build();
        }
        throw new IllegalArgumentException("unsupported callee type :" + ObjectUtil.simpleClassName(servant));
    }

    @Override
    public ScopedModule lookupModule(InputMessage inputMessage) {
        HttpDuplexRequest httpRequest = (HttpDuplexRequest) inputMessage;
        ScopedPlatform platform = inputMessage.channel()
                .platform();
        if (platform.applications().size() == 1) {
            ScopedApplication application = platform.applications().iterator().next();
            if (application.modules().size() == 1) {
                return application.modules().iterator().next();
            }
        }
        HttpHeaders headers = httpRequest.headers();
        String defaultName = Constant.DEFAULT_NAME;
        // todo 优化没有application直接报错并返回给客户端
        CharSequence applicationName = headers.getOrDefault(KeyConstant.REQUEST_REMOTE_APPLICATION, defaultName);
        CharSequence moduleName = headers.getOrDefault(KeyConstant.REQUEST_REMOTE_MODULE, defaultName);
        return inputMessage.channel()
                .platform()
                .lookupApplication(applicationName.toString())
                .lookupModule(moduleName.toString());
    }

    @Override
    public void sendCalleeNotFound(InputMessage inputMessage) {
        //        HttpResponse httpResponse = create404Response(request, channel);
        //        channel.send(httpResponse);
    }

    private HttpResponse create404Response(Request request, Channel channel) {
        EffiRpcException ex = InteractionErrorCodes.SERVANT_NOT_FOUND.fail(request.url().baseUrl());
        HttpHeaders headers = version().newHeaders();
        headers.add(RESPONSE_REQUEST_HEADERS.entrySet());
        headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        return HttpDuplexResponse.builder()
                .version(version)
                .method(HttpMethod.GET)
                .statusCode(404)
                .url(request.url())
                .headers(headers)
                .body(ex.getMessage().getBytes())
                .build();
    }

    @Override
    public Class<? extends Request> requestType() {
        return HttpRequest.class;
    }

    @Override
    public Class<? extends Response> responseType() {
        return HttpResponse.class;
    }

    public HttpVersion version() {
        return version;
    }

    private ClientExchangeContextCodec createClientCodec() {
        HttpClientCodec clientCodec = new HttpClientCodec();
        return new ConfigurableClientCodec<HttpRequest, HttpResponse>()
                .encoder(clientCodec)
                .decoder(clientCodec)
                .resultExtractor(this::extractResult);
    }

    private ServerExchangeContextCodec createServerCodec() {
        HttpServerCodec serverCodec = new HttpServerCodec();
        return new ConfigurableServerCodec<HttpResponse, HttpRequest>()
                .encoder(serverCodec)
                .decoder(serverCodec);
    }

    private Interaction.Result extractResult(HttpResponse response) {
        if (response.succeeded()) {
            return Interaction.Result.success(response.url(), response.body());
        }
        return Interaction.Result.failure(response.url(), response.cause());
    }

}
