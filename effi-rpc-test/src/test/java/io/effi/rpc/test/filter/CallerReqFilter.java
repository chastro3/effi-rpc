package io.effi.rpc.test.filter;

import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.filter.InvokeFilter;
import io.effi.rpc.protocol.http.h2.Http2Callee;

/**
 * @Author WenBo Zhou
 * @Date 2025/1/20 11:10
 */
public class CallerReqFilter implements InvokeFilter<Envelope.Request, Http2Callee<?>> {
    @Override
    public Result doFilter(InvocationContext<Envelope.Request, Http2Callee<?>> context) {
        return null;
    }
}
