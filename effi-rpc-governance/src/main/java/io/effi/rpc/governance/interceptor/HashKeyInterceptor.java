package io.effi.rpc.governance.interceptor;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.context.options.GovernanceOptions;

import static io.effi.rpc.governance.interceptor.HashKeyInterceptor.NAME;

/**
 * Resolves the consistent-hash key from a declared invocation argument before service location.
 */
@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class HashKeyInterceptor implements Interceptor.CallUnit<Request, Caller<?>> {

    public static final String NAME = "hashKeyInterceptor";

    @Override
    public Interaction.Result intercept(CallContext<Request, Caller<?>> context, Chain chain) {
        Integer index = context.peer().option(GovernanceOptions.HASH_KEY_INDEX);
        if (index != null && index >= 0) {
            Object[] args = context.args();
            if (index < args.length && args[index] != null) {
                context.set(KeyConstant.HASH_KEY, String.valueOf(args[index]));
            }
        }
        return chain.proceed(context);
    }

    @Override
    public UnitType<Request, Caller<?>> unitType() {
        return UnitType.cached(Request.class, Caller.class);
    }
}
