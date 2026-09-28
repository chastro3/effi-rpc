package io.effi.rpc.transport.netty;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.options.TransportOptions;
import io.effi.rpc.option.OptionName;
import io.netty.channel.WriteBufferWaterMark;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NettySupportTest {

    @Test
    void writeBufferWaterMarkClampsInvalidHighValue() {
        EndpointConfig config = proxy(EndpointConfig.class, (proxy, method, args) -> {
            if ("option".equals(method.getName())) {
                OptionName<?> option = (OptionName<?>) args[0];
                if (option == TransportOptions.WRITE_BUFFER_LOW_WATER_MARK) {
                    return 4096;
                }
                if (option == TransportOptions.WRITE_BUFFER_HIGH_WATER_MARK) {
                    return 1024;
                }
            }
            return defaultValue(method.getReturnType());
        });

        WriteBufferWaterMark watermark = NettySupport.newWriteBufferWaterMark(config);

        assertEquals(4096, watermark.low());
        assertEquals(4096, watermark.high());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        return 0D;
    }
}
