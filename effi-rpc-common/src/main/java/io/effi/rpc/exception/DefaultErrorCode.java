package io.effi.rpc.exception;

import io.effi.rpc.util.AssertUtil;

/**
 * Provides the default implementation of {@link ErrorCode}.
 */
public class DefaultErrorCode implements ErrorCode {

    private final String code;

    private final String message;

    DefaultErrorCode(String code, String message) {
        this.code = AssertUtil.notBlank(code, "code");
        this.message = AssertUtil.notBlank(message, "message");
    }

    public static DefaultErrorCode valueOf(String code, String message) {
        return new DefaultErrorCode(code, message);
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
