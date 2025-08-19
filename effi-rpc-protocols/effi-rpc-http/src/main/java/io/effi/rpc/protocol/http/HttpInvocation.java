package io.effi.rpc.protocol.http;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.parameter.Argument;
import io.effi.rpc.context.parameter.Body;
import io.effi.rpc.context.parameter.Header;
import io.effi.rpc.context.parameter.ParamVar;
import io.effi.rpc.context.parameter.PathVar;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.util.CollectionUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps HTTP request arguments including URL, headers, and body.
 */
public class HttpInvocation {

    private final SmartURL requestSmartUrl;

    private final Map<String, String> headers;

    private final Object body;

    public HttpInvocation(Caller<?> caller, Object[] args) {
        ArgumentGroup grouped = groupArguments(args);
        this.requestSmartUrl = createUrl(caller, grouped.pathVars, grouped.paramVars);
        this.headers = grouped.headers;
        this.body = grouped.body;
    }

    private ArgumentGroup groupArguments(Object[] args) {
        Map<String, String> pathVars = new HashMap<>();
        Map<String, String> paramVars = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Object body = null;
        if (CollectionUtil.isNotEmpty(args)) {
            for (Object arg : args) {
                if (arg instanceof PathVar<?> pathVar
                        && pathVar.get() instanceof Argument.Target target) {
                    pathVars.putAll(target.get());
                } else if (arg instanceof ParamVar<?> paramVar
                        && paramVar.get() instanceof Argument.Target target) {
                    paramVars.putAll(target.get());
                } else if (arg instanceof Header<?> headerVar
                        && headerVar.get() instanceof Argument.Target target) {
                    headers.putAll(target.get());
                } else if (arg instanceof Body<?> bodyVar && body == null) {
                    body = bodyVar.get();
                }
            }
        }
        return new ArgumentGroup(pathVars, paramVars, headers, body);
    }

    private SmartURL createUrl(Caller<?> caller, Map<String, String> pathVars, Map<String, String> paramVars) {
        QueryPath queryPath = caller.queryPath();
        SmartURL.Builder urlBuilder = SmartURL.builder()
                .scheme(caller.protocol().name())
                .withQueryParams(paramVars);
        if (queryPath != null) {
            String[] realPath = queryPath.render(pathVars);
            if (CollectionUtil.isEmpty(realPath)) {
                urlBuilder.path(QueryPath.empty());
            } else {
                urlBuilder.path(QueryPath.valueOf(Arrays.asList(realPath)));
            }
        }
        return urlBuilder.build();
    }

    public Map<String, String> headers() {
        return headers;
    }

    public SmartURL requestUrl() {
        return requestSmartUrl;
    }

    public Object body() {
        return body;
    }

    private record ArgumentGroup(
            Map<String, String> pathVars,
            Map<String, String> paramVars,
            Map<String, String> headers,
            Object body
    ) {}
}
