package io.effi.rpc.protocol.http.support;

import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.constant.Component.Serialization.*;

/**
 * Maps content types to corresponding serialization formats.
 */
public enum MediaType {

    APPLICATION_TEXT("application/text", "txt"),
    APPLICATION_JSON("application/json", JSON),
    APPLICATION_MSGPACK("application/msgpack", MSGPACK),
    APPLICATION_PROTOBUF("application/protobuf", PROTOBUF);

    private final CharSequence contentType;

    private final String serialization;

    MediaType(String contentType, String serialization) {
        this.contentType = contentType;
        this.serialization = serialization;
    }

    public static MediaType fromName(CharSequence contentType) {
        if (contentType == null) return null;
        for (MediaType value : MediaType.values()) {
            if (StringUtil.equals(contentType, value.contentType())) {
                return value;
            }
        }
        return null;
    }

    public static MediaType fromSerialization(String serialization) {
        if (serialization == null) return null;
        for (MediaType value : MediaType.values()) {
            if (StringUtil.equals(serialization, value.serialization())) {
                return value;
            }
        }
        return null;
    }

    public CharSequence contentType() {
        return contentType;
    }

    public String serialization() {
        return serialization;
    }

}
