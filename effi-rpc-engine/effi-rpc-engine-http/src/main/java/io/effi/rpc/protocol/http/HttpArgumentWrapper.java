package io.effi.rpc.protocol.http;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.url.*;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.parameter.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpArgumentWrapper {

    private Map<String, String> headers;

    private URL requestUrl;

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
        URLBuilder urlBuilder = URL.builder()
                .type(URLType.REQUEST)
                .protocol(caller.protocol())
                .address(Constant.UNKNOWN_ADDRESS);
        if (queryPath != null) {
            List<String> paths = queryPath.paths();
            if (CollectionUtil.isNotEmpty(paths)) {
                for (int i = 0; i < paths.size(); i++) {
                    String path = paths.get(i);
                    String var = URLUtil.getVar(path);
                    if (!StringUtil.isBlank(var)) {
                        paths.add(i, pathVars.getOrDefault(var, path));
                    }
                }
                urlBuilder.paths(paths);
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
