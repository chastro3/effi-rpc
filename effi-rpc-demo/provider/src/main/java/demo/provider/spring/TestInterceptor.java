package demo.provider.spring;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Request;
import org.springframework.stereotype.Component;


@Component
public class TestInterceptor implements Interceptor.CallUnit<Request, Caller<?>>{

    @Override
    public Interaction.Result intercept(CallContext<Request, Caller<?>> context, Chain chain) {
        return null;
    }
}
