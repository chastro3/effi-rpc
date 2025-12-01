package io.effi.rpc.serialization.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.serialization.AbstractSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import static io.effi.rpc.serialization.json.JacksonSerializer.NAME;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using Jackson.
 */
@Extension(value = NAME, onClass = "com.fasterxml.jackson.databind.ObjectMapper")
public class JacksonSerializer extends AbstractSerializer {

    public static final String NAME = "jackson";

    protected JsonMapper jsonMapper;

    public JacksonSerializer() {
        this.jsonMapper = JsonMapper.builder()
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .visibility(
                        VisibilityChecker.Std.defaultInstance()
                                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                )
                .build();
    }

    @Override
    protected void doSerialize(Object obj, OutputStream out) throws IOException {
        jsonMapper.writeValue(out, obj);
    }

    @Override
    protected Object doDeserialize(InputStream in, Type type) throws IOException {
        if (type == String.class || type == Object.class) {
            return jsonMapper.readValue(in, String.class);
        }
        return jsonMapper.readValue(in, jsonMapper.constructType(type));
    }

    public JsonMapper jsonMapper() {
        return jsonMapper;
    }
}
