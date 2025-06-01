package io.effi.rpc.serialization.msgpack;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.effi.rpc.serialization.json.JacksonSerializer;
import io.effi.rpc.annotation.spi.Extension;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import static io.effi.rpc.constant.Component.Serialization.MSGPACK;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using MessagePack.
 */
@Extension(value = MSGPACK, onClass = "org.msgpack.jackson.dataformat.MessagePackFactory")
public class MsgPackSerializer extends JacksonSerializer {

    public MsgPackSerializer() {
        super.jsonMapper = JsonMapper.builder(new MessagePackFactory())
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .visibility(
                        VisibilityChecker.Std.defaultInstance()
                                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build();
    }

}
