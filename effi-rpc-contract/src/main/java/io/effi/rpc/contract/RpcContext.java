package io.effi.rpc.contract;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.extension.AbstractAttributes;
import io.effi.rpc.common.extension.StringAccessor;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.url.URLUtil;
import io.effi.rpc.common.util.StringUtil;

import java.util.Map;

/**
 * Rpc context.
 */
public class RpcContext {

    private static final ThreadLocal<CurrentContext> CURRENT_CONTEXT = ThreadLocal.withInitial(CurrentContext::new);

    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = ThreadLocal.withInitial(RequestContext::new);

    private static final ThreadLocal<ResponseContext> RESPONSE_CONTEXT = ThreadLocal.withInitial(ResponseContext::new);

    /**
     * Request Context.
     * <p>It will be transparently transmitted to the server</p>
     *
     * @return
     */
    public static RequestContext requestContext() {
        return REQUEST_CONTEXT.get();
    }

    /**
     * Response Context.
     * <p>It will be transparently transmitted to the client</p>
     *
     * @return
     */
    public static ResponseContext responseContext() {
        return RESPONSE_CONTEXT.get();
    }

    /**
     * Current Context.
     * <p>Context within the local thread</p>
     *
     * @return
     */
    public static CurrentContext currentContext() {
        return CURRENT_CONTEXT.get();
    }

    /**
     * Clear the all context.
     */
    public static void clear() {
        currentContext().clear();
        requestContext().clear();
        responseContext().clear();

    }

    /**
     * Current Context.
     */
    public static class CurrentContext extends AbstractAttributes {

        /**
         * Clear context.
         */
        @Override
        public void clear() {
            super.clear();
            CURRENT_CONTEXT.remove();
        }
    }

    /**
     * Request Context.
     */
    public static class RequestContext extends StringAccessor<RequestContext> {

        /**
         * Parse the request context in the URL.
         *
         * @param url
         */
        public static void parse(URL url) {
            String requestContextStr = url.getParam(KeyConstant.REQUEST_CONTEXT);
            if (!StringUtil.isBlank(requestContextStr)) {
                for (Map.Entry<String, String> entry : URLUtil.parseQueryParam(requestContextStr).entrySet()) {
                    requestContext().set(entry.getKey(), entry.getValue());
                }
            }
        }

        /**
         * Clear context.
         */
        @Override
        public void clear() {
            super.clear();
            REQUEST_CONTEXT.remove();
        }

        @Override
        public String toString() {
            return URLUtil.toQueryParam(accessor);
        }
    }

    /**
     * Response Context.
     */
    public static class ResponseContext extends StringAccessor<ResponseContext> {

        /**
         * Parse the response context in the URL.
         *
         * @param url
         */
        public static void parse(URL url) {
            String responseContextStr = url.getParam(KeyConstant.RESPONSE_CONTEXT);
            if (!StringUtil.isBlank(responseContextStr)) {
                for (Map.Entry<String, String> entry : URLUtil.parseQueryParam(responseContextStr).entrySet()) {
                    responseContext().set(entry.getKey(), entry.getValue());
                }
            }
        }

        /**
         * Clear context.
         */
        @Override
        public void clear() {
            super.clear();
            RESPONSE_CONTEXT.remove();
        }

        @Override
        public String toString() {
            return URLUtil.toQueryParam(accessor);
        }
    }

}
