package io.effi.rpc.protocol.http.filter;


import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.filter.FilterType;
import io.effi.rpc.base.filter.InvokeFilter;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.protocol.http.filter.HttpRequestFilter.NAME;

@Extension(NAME)
public class HttpRequestFilter implements InvokeFilter<HttpRequest<Object>, Caller<?>> {

    public static final String NAME = "httpRequestFilter";

    @Override
    public Result doFilter(InvocationContext<HttpRequest<Object>, Caller<?>> context) {
        HttpRequest<Object> request = context.envelope();
        Caller<?> caller = context.invoker();
        HttpHeaders headers = request.headers();
        String remoteApplication = caller.get(DefaultConfigKeys.REMOTE_APPLICATION);
        if (StringUtil.isNotBlank(remoteApplication)) {
            headers.add(KeyConstant.REQUEST_REMOTE_APPLICATION, remoteApplication);
        }
        String remoteModule = caller.get(DefaultConfigKeys.REMOTE_MODULE);
        if (StringUtil.isNotBlank(remoteModule)) {
            headers.add(KeyConstant.REQUEST_REMOTE_MODULE, remoteModule);
        }
        return context.execute();
    }

    @Override
    public FilterType<HttpRequest<Object>, Caller<?>> type() {
        return FilterType.of(HttpRequest.class, Caller.class);
    }
}
