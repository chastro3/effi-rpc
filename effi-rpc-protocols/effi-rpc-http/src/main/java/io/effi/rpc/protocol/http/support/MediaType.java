package io.effi.rpc.protocol.http.support;

import io.effi.rpc.serialization.json.JacksonSerializer;
import io.effi.rpc.util.StringUtil;

/**
 * Maps content types to corresponding serialization formats.
 */
public enum MediaType {

    APPLICATION_TEXT("application/text", "txt"),
    APPLICATION_JSON("application/json", JacksonSerializer.NAME),
    // todo 优化
    APPLICATION_MSGPACK("application/msgpack",""),
    APPLICATION_PROTOBUF("application/protobuf", "");

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
