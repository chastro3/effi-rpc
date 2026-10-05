package io.effi.rpc.spring;

import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.spring.properties.EffiRpcProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EffiRpcPropertiesTest {

    @Test
    void bindsConsumerAndProviderStructure() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("effi.rpc.consumer.common.protocol", Http1Protocol.NAME);
        values.put("effi.rpc.consumer.common.timeout", "3s");
        values.put("effi.rpc.consumer.targets.order-service.interfaces[0]", TestConsumer.class.getName());
        values.put("effi.rpc.consumer.targets.order-service.endpoint", "order-service");
        values.put("effi.rpc.provider.common.protocols[0]", Http1Protocol.NAME);

        EffiRpcProperties properties = new Binder(new MapConfigurationPropertySource(values))
                .bind("effi.rpc", Bindable.of(EffiRpcProperties.class))
                .get();

        assertEquals(Http1Protocol.NAME, properties.consumer().common().protocol());
        assertEquals(Duration.ofSeconds(3), properties.consumer().common().timeout());
        EffiRpcProperties.ConsumerTarget target =
                properties.consumer().targets().get("order-service");
        assertNotNull(target);
        assertEquals("order-service", target.endpoint());
        assertEquals(TestConsumer.class, target.interfaces().get(0));
        assertEquals(Http1Protocol.NAME, properties.provider().common().protocols().get(0));
    }

    @Test
    void serverHostDefaultsToRoutableAddress() {
        EffiRpcProperties.Server server =
                new EffiRpcProperties.Server(null, null, null, null, null);
        assertNotNull(server.host());
        assertNotEquals("0.0.0.0", server.host());
    }

    interface TestConsumer {
        String hello(String name);
    }
}
