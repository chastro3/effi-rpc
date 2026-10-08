package io.effi.rpc.serialization.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.serialization.AbstractSerializer;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import static io.effi.rpc.serialization.json.JacksonSerializer.NAME;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using Jackson.
 */
@Extension(value = NAME, onClass = "tools.jackson.databind.ObjectMapper")
public class JacksonSerializer extends AbstractSerializer {

    public static final String NAME = "jackson";

    protected JsonMapper jsonMapper;

    public JacksonSerializer() {
        this.jsonMapper = JsonMapper.builder()
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .changeDefaultVisibility(
                        visibilityChecker -> visibilityChecker
                                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                )
                .build();
    }

    @Override
    public Object[] deserialize(InputStream in, Type[] types) throws IOException {
        try (JsonParser parser = jsonMapper.tokenStreamFactory().createParser(in)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected JSON array");
            }
            Object[] values = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                JsonToken token = parser.nextToken();
                if (token == JsonToken.END_ARRAY) {
                    throw new IOException("JSON array contains fewer values than parameters");
                }
                values[i] = token == JsonToken.VALUE_NULL
                        ? null
                        : jsonMapper.readerFor(jsonMapper.constructType(types[i]))
                        .without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                        .readValue(parser);
            }
            if (parser.nextToken() != JsonToken.END_ARRAY) {
                throw new IOException("JSON array contains more values than parameters");
            }
            return values;
        }
    }

    /**
     * Returns the underlying Jackson mapper.
     */
    public JsonMapper jsonMapper() {
        return jsonMapper;
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
}
