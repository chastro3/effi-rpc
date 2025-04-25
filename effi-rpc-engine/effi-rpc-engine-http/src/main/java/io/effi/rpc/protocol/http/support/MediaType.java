package io.effi.rpc.protocol.http.support;

import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.constant.Component.Serialization.*;

/**
 * Http media type.
 */
public enum MediaType {

    APPLICATION_TEXT("application/text", "txt"),
    APPLICATION_JSON("application/json", JSON),
    APPLICATION_MSGPACK("application/msgpack", MSGPACK),
    APPLICATION_PROTOBUF("application/protobuf", PROTOBUF);

    private final CharSequence name;

    private final String serialization;

    MediaType(String name, String serialization) {
        this.name = name;
        this.serialization = serialization;
    }

    /**
     * Gets MediaType by name.
     *
     * @param name
     * @return
     */
    public static MediaType fromName(CharSequence name) {
        if (name == null) return null;
        for (MediaType value : MediaType.values()) {
            if (StringUtil.equals(name, value.getName())) {
                return value;
            }
        }
        return null;
    }

    /**
     * Gets MediaType by serialization.
     *
     * @param serialization
     * @return
     */
    public static MediaType fromSerialization(String serialization) {
        if (serialization == null) return null;
        for (MediaType value : MediaType.values()) {
            if (StringUtil.equals(serialization, value.getSerialization())) {
                return value;
            }
        }
        return null;
    }

    public CharSequence getName() {
        return name;
    }

    public String getSerialization() {
        return serialization;
    }

}
