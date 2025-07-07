package io.effi.rpc.protocol.http;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.parameter.Argument;
import io.effi.rpc.base.parameter.Body;
import io.effi.rpc.base.parameter.Header;
import io.effi.rpc.base.parameter.ParamVar;
import io.effi.rpc.base.parameter.PathVar;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.config.URLUtil;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps HTTP request arguments including URL, headers, and body.
 */
public class HttpInvocation {

    private final URL requestUrl;

    private final Map<String, String> headers;

    private final Object body;

    public HttpInvocation(Caller<?> caller, Object[] args) {
        ArgumentGroup grouped = groupArguments(args);
        this.requestUrl = createUrl(caller, grouped.pathVars, grouped.paramVars);
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

    private URL createUrl(Caller<?> caller, Map<String, String> pathVars, Map<String, String> paramVars) {
        QueryPath queryPath = caller.queryPath();
        URL.Builder urlBuilder = URL.builder()
                .type(URLType.REQUEST)
                .protocol(caller.protocol())
                .address(Constant.UNKNOWN_ADDRESS);
        if (queryPath != null) {
            urlBuilder.paths(resolvePaths(queryPath.paths(), pathVars));
            if (CollectionUtil.isNotEmpty(queryPath.queryParams())) {
                urlBuilder.params(queryPath.queryParams());
            }
            if (CollectionUtil.isNotEmpty(paramVars)) {
                urlBuilder.params(paramVars);
            }
        }

        return urlBuilder.build();
    }

    private List<String> resolvePaths(List<String> paths, Map<String, String> pathVars) {
        // todo path 解析和匹配
        if (CollectionUtil.isEmpty(paths)) {
            return Collections.emptyList();
        }
        List<String> resolved = new ArrayList<>(paths.size());
        for (String path : paths) {
            String var = URLUtil.getVar(path);
            resolved.add(!StringUtil.isBlank(var) ? pathVars.getOrDefault(var, path) : path);
        }
        return resolved;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public URL requestUrl() {
        return requestUrl;
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
