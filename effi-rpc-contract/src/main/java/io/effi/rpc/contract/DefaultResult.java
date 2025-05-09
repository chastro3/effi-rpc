package io.effi.rpc.contract;

import io.effi.rpc.config.URL;
import io.effi.rpc.util.AssertUtil;

/**
 * Default implementation of {@link Result}.
 */
record DefaultResult(URL url, ResultType<?> type, Object value) implements Result {

    public DefaultResult(URL url, ResultType<?> type, Object value) {
        this.url = AssertUtil.notNull(url, "url");
        this.type = AssertUtil.notNull(type, "type");
        this.value = value;
    }
}

