package demo.provider;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.filter.FilterType;
import io.effi.rpc.base.filter.InvokeFilter;

public class CalleeLogFilter implements InvokeFilter<Envelope.Request, Callee<?>> {

    @Override
    public Result doFilter(InvocationContext<Envelope.Request, Callee<?>> context) {
        System.out.println("请求地址========>> " + context.envelope().url());
        return context.execute();
    }

    public void ppHlleo(){
        System.out.println("cccc");
    }

    @Override
    public FilterType<Envelope.Request, Callee<?>> type() {
        return FilterType.of(Envelope.Request.class, Callee.class);
    }
}
