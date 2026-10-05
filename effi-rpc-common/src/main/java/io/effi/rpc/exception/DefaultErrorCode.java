package io.effi.rpc.exception;

import io.effi.rpc.util.AssertUtil;

import java.util.Objects;

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

    /**
     * Creates a default error code.
     *
     * @param code error code
     * @param message error message
     * @return default error code
     */
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DefaultErrorCode other)) {
            return false;
        }
        return code.equals(other.code) && message.equals(other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }
}
