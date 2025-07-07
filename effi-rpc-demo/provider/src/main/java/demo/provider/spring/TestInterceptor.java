package demo.provider.spring;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallInterceptor;
import io.effi.rpc.base.context.InterceptorChain;
import org.springframework.stereotype.Component;


@Component
public class TestInterceptor implements CallInterceptor<Message.Request, Caller<?>> {

    @Override
    public Result intercept(CallContext<Message.Request, Caller<?>> context, InterceptorChain chain) {
        return null;
    }
}
