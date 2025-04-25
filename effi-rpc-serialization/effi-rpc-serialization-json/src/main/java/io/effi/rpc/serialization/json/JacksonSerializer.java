package io.effi.rpc.serialization.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.effi.rpc.constant.Component;
import io.effi.rpc.spi.Extension;
import io.effi.rpc.serialization.AbstractSerializer;

import java.lang.reflect.Type;

/**
 * {@link io.effi.rpc.serialization.Serializer} implementation based on jackson.
 */
@Extension(Component.Serialization.JSON)
public class JacksonSerializer extends AbstractSerializer {

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
    protected byte[] doSerialize(Object input) throws Exception {
        return jsonMapper.writeValueAsBytes(input);
    }

    @Override
    protected Object doDeserialize(byte[] bytes, Type type) throws Exception {
        if (type == String.class || type == Object.class) {
            return jsonMapper.readValue(bytes, String.class);
        }
        return jsonMapper.readValue(bytes, jsonMapper.constructType(type));
    }

    /**
     * Gets used {@link JsonMapper}.
     *
     * @return
     */
    public JsonMapper jsonMapper() {
        return jsonMapper;
    }

}
