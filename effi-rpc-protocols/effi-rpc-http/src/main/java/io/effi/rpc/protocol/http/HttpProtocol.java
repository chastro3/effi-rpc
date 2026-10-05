package io.effi.rpc.protocol.http;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.protocol.http.codec.HttpClientCodec;
import io.effi.rpc.protocol.http.codec.HttpServerCodec;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpMessage;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.AbstractProtocol;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.codec.ConfigurableClientCodec;
import io.effi.rpc.transport.codec.ConfigurableServerCodec;
import io.effi.rpc.transport.codec.ServerExchangeContextCodec;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.ObjectUtil;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.Arrays;
import java.util.Map;


/**
 * Provides a standard http implementation of {@link TransportProtocol}.
 */
public abstract class HttpProtocol extends AbstractProtocol {

    private static final Map<CharSequence, CharSequence> RESPONSE_REQUEST_HEADERS = HttpUtil.regularResponseHeaders();

    private final HttpVersion version;

    protected HttpProtocol(HttpVersion version) {
        this.version = AssertUtil.notNull(version, "version");
        initialize(version().name(), ProtocolStack.TCP, createServerCodec(), createClientCodec());
    }

    @Override
    public Request createRequest(Caller<?> caller, Invocation invocation) {
        if (caller instanceof HttpCaller<?> httpCaller) {
            Map<String, String> pathVariables = invocation.get(HttpInvocationKeys.PATH_VARIABLES);
            Map<String, String> queryParameters = invocation.get(HttpInvocationKeys.QUERY_PARAMETERS);
            Map<String, String> argumentHeaders = invocation.get(HttpInvocationKeys.HEADERS);
            HttpHeaders headers = version().newHeaders();
            HttpUtil.addRegularRequestHeaders(headers, caller.platform());
            if (CollectionUtil.isNotEmpty(argumentHeaders)) {
                headers.add(argumentHeaders.entrySet());
            }
            HttpUtil.addContentType(headers, caller.options());
            HttpUtil.addContentEncoding(headers, caller.options());
            return HttpDuplexRequest.builder()
                    .version(version)
                    .method(httpCaller.httpMethod())
                    .url(createRequestUrl(caller, pathVariables, queryParameters))
                    .headers(headers)
                    .body(requestBody(invocation))
                    .build();
        }
        throw new IllegalArgumentException(Messages.unSupport("caller", caller.getClass()));
    }

    @Override
    public Response createResponse(Servant servant, Request request, Interaction.Result result) {
        if (servant instanceof HttpServant httpCallee) {
            int statusCode = 200;
            Object value = result.value();
            if (!result.succeeded()) {
                EffiRpcException cause = result.cause();
                value = cause.getMessage();
                statusCode = errorStatus(cause);
            }
            HttpHeaders headers = version().newHeaders();
            headers.add(RESPONSE_REQUEST_HEADERS.entrySet());
            HttpUtil.addContentType(headers, servant.options());
            HttpMessage httpRequest = request instanceof HttpMessage message ? message : null;
            HttpUtil.addContentEncoding(headers, servant.options(), httpRequest);
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
        ScopedApplication application = inputMessage.channel()
                .platform()
                .lookupApplication(applicationName.toString());
        return application == null ? null : application.lookupModule(moduleName.toString());
    }

    @Override
    public Response createErrorResponse(InputMessage inputMessage, EffiRpcException cause) {
        if (!(inputMessage instanceof HttpRequest request)) {
            throw new IllegalArgumentException("Expected an HTTP request but received " + ObjectUtil.simpleClassName(inputMessage));
        }
        int statusCode = errorStatus(cause);
        HttpHeaders headers = version().newHeaders();
        headers.add(RESPONSE_REQUEST_HEADERS.entrySet());
        headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        return HttpDuplexResponse.builder()
                .version(version)
                .method(request.method())
                .statusCode(statusCode)
                .url(request.url())
                .headers(headers)
                .body(cause.getMessage())
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

    private SmartURL createRequestUrl(Caller<?> caller, Map<String, String> pathVariables, Map<String, String> queryParameters) {
        String[] realPath = caller.queryPath().render(pathVariables);
        return SmartURL.builder()
                .scheme(caller.protocol().name())
                .queryParams(queryParameters)
                .path(QueryPath.valueOf(Arrays.asList(realPath)))
                .build();
    }

    private Object requestBody(Invocation invocation) {
        return invocation.arguments().isEmpty()
                ? invocation.get(HttpInvocationKeys.BODY)
                : invocation.arguments().values();
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
                .decoder(serverCodec)
                .invocationResolver(new HttpInvocationResolver());
    }

    private Interaction.Result extractResult(HttpResponse response) {
        if (response.succeeded()) {
            return Interaction.Result.success(response.url(), response.body());
        }
        return Interaction.Result.failure(response.url(), response.cause());
    }

    private static int errorStatus(EffiRpcException cause) {
        if (cause.errorCode() == InteractionErrorCodes.SERVANT_NOT_FOUND) {
            return 404;
        }
        if (cause.errorCode() == PredefinedErrorCode.SERVICE_UNAVAILABLE
                || cause.errorCode() == InteractionErrorCodes.SERVER_OVERLOADED) {
            return 503;
        }
        return 500;
    }

}
