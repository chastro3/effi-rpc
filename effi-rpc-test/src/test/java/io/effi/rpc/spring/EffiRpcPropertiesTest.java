package io.effi.rpc.spring;

import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.spring.autoconfigure.EffiRpcProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EffiRpcPropertiesTest {

    @Test
    void bindsConsumerAndProviderStructure() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("effi.rpc.consumer.protocol", Http1Protocol.NAME);
        values.put("effi.rpc.consumer.timeout", "3s");
        values.put("effi.rpc.consumer.compressor", "gzip");
        values.put("effi.rpc.provider.protocols[0]", Http1Protocol.NAME);

        EffiRpcProperties properties = new Binder(new MapConfigurationPropertySource(values))
                .bind("effi.rpc", Bindable.of(EffiRpcProperties.class))
                .get();

        assertEquals(Http1Protocol.NAME, properties.consumer().protocol());
        assertEquals(Duration.ofSeconds(3), properties.consumer().timeout());
        assertEquals("gzip", properties.consumer().compressor());
        assertEquals(Http1Protocol.NAME, properties.provider().protocols().get(0));
    }

    @Test
    void bindsCommaSeparatedNames() {
        EffiRpcProperties properties = new Binder(new MapConfigurationPropertySource(Map.of(
                "effi.rpc.provider.protocols", "http/1.1,http/2",
                "effi.rpc.consumer.registries", "consul,nacos"
        ))).bind("effi.rpc", Bindable.of(EffiRpcProperties.class)).get();

        assertEquals(List.of("http/1.1", "http/2"), properties.provider().protocols());
        assertEquals(List.of("consul", "nacos"), properties.consumer().registries());
    }

    @Test
    void serverHostDefaultsToRoutableAddress() {
        EffiRpcProperties.Server server =
                new EffiRpcProperties.Server(null, null, null, null, null);
        assertNotNull(server.host());
        assertNotEquals("0.0.0.0", server.host());
    }

}
