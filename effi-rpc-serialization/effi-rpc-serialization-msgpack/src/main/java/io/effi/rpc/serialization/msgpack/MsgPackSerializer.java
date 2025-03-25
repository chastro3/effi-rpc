package io.effi.rpc.serialization.msgpack;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.serialization.json.JacksonSerializer;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import static io.effi.rpc.common.constant.Component.Serialization.MSGPACK;

/**
 * {@link io.effi.rpc.serialization.Serializer} implementation based on message pack.
 */
@Extension(MSGPACK)
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
