package io.effi.rpc.protocol.http;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.parameter.Argument;
import io.effi.rpc.base.parameter.Body;
import io.effi.rpc.base.parameter.Header;
import io.effi.rpc.base.parameter.ParamVar;
import io.effi.rpc.base.parameter.PathVar;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.config.URLUtil;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps HTTP request arguments including URL, headers, and body.
 */
public class HttpArgumentWrapper {

    private final URL requestUrl;

    private Map<String, String> headers;

    private Object body;

    public HttpArgumentWrapper(Caller<?> caller, Object[] args) {
        Map<String, String> pathVars = new HashMap<>();
        Map<String, String> paramVars = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        if (CollectionUtil.isNotEmpty(args)) {
            for (Object arg : args) {
                if (arg instanceof PathVar<?> pathVar && pathVar.get() instanceof Argument.Target target) {
                    pathVars.putAll(target.get());
                } else if (arg instanceof ParamVar<?> paramVar && paramVar.get() instanceof Argument.Target target) {
                    paramVars.putAll(target.get());
                } else if (arg instanceof Body<?> bodyVar && body == null) {
                    this.body = bodyVar.get();
                } else if (arg instanceof Header<?> headerVar && headerVar.get() instanceof Argument.Target target) {
                    headers.putAll(target.get());
                }
            }
        }
        QueryPath queryPath = caller.queryPath();
        URL.Builder urlBuilder = URL.builder()
                .type(URLType.REQUEST)
                .protocol(caller.protocol())
                .address(Constant.UNKNOWN_ADDRESS);
        if (queryPath != null) {
            List<String> paths = queryPath.paths();
            ArrayList<String> newPaths = new ArrayList<>(paths.size());
            if (CollectionUtil.isNotEmpty(paths)) {
                for (String path : paths) {
                    String var = URLUtil.getVar(path);
                    if (!StringUtil.isBlank(var)) {
                        newPaths.add(pathVars.getOrDefault(var, path));
                    } else {
                        newPaths.add(path);
                    }
                }
                urlBuilder.paths(newPaths);
            }
            Map<String, String> queryParams = queryPath.queryParams();
            if (CollectionUtil.isNotEmpty(queryParams)) {
                urlBuilder.params(queryParams);
            }
            if (CollectionUtil.isNotEmpty(paramVars)) {
                urlBuilder.params(paramVars);
            }
        }
        this.requestUrl = urlBuilder.build();
        String remoteApplication = caller.get(DefaultConfigKeys.REMOTE_APPLICATION);
        if (StringUtil.isNotBlank(remoteApplication)) {
            requestUrl.addParam(KeyConstant.REQUEST_REMOTE_APPLICATION, remoteApplication);
        }
        String remoteModule = caller.get(DefaultConfigKeys.REMOTE_MODULE);
        if (StringUtil.isNotBlank(remoteModule)) {
            requestUrl.addParam(KeyConstant.REQUEST_REMOTE_MODULE, remoteModule);
        }
        if (!headers.isEmpty()) {
            this.headers = headers;
        }
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
}
