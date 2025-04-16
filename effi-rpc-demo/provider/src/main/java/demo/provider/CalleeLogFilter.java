package demo.provider;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.filter.InvokeFilter;

public class CalleeLogFilter implements InvokeFilter<Envelope.Request, Callee<?>> {

    @Override
    public Result doFilter(InvocationContext<Envelope.Request, Callee<?>> context) {
        System.out.println("请求地址========>> " + context.source().url());
        return context.execute();
    }

    public void ppHlleo(){
        System.out.println("cccc");
    }
}
