package io.effi.rpc.protocol.http.arg.binder;

import io.effi.rpc.context.parameter.Argument;
import io.effi.rpc.context.parameter.Body;
import io.effi.rpc.context.parameter.Header;
import io.effi.rpc.context.parameter.ParamVar;
import io.effi.rpc.context.parameter.ParameterBinder;
import io.effi.rpc.context.parameter.PathVar;

/**
 * Creates HTTP parameter binders from explicit argument mappings.
 */
public final class HttpParameterBinders {

    private HttpParameterBinders() {
    }

    public static ParameterBinder bind(Argument argument) {
        if (argument instanceof PathVar<?> pathVar
                && pathVar.get() instanceof Argument.Source source) {
            return new HttpPathParameterBinder(source.get(), null);
        }
        if (argument instanceof ParamVar<?> paramVar
                && paramVar.get() instanceof Argument.Source source) {
            return new HttpQueryParameterBinder(source.get(), null);
        }
        if (argument instanceof Header<?> header
                && header.get() instanceof Argument.Source source) {
            return new HttpHeaderParameterBinder(source.get(), null);
        }
        if (argument instanceof Body<?>) {
            return HttpBodyParameterBinder.INSTANCE;
        }
        throw new IllegalArgumentException("Unsupported HTTP argument mapping: " + argument);
    }
}
