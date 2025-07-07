package io.effi.rpc.base.context;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;

/**
 * Intercepts the {@link ReplyContext} during the reply phase.
 * <p>
 * For the client, invoked after receiving the response and before returning it to the caller.
 * For the server, invoked after processing the request and before sending the response.
 * </p>
 *
 * @see Interceptor
 * @see ReplyContext
 */
public interface ReplyInterceptor<R extends Message.Response, C extends Caller<?>>
        extends Interceptor<R, C, ReplyContext<R, C>>, ReplyExecutionUnit<R, C, InterceptorChain> {}

