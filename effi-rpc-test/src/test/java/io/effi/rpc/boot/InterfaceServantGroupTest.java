package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.transport.TransportProtocol;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterfaceServantGroupTest {

    private static final AtomicInteger PLATFORM_IDS = new AtomicInteger();

    @Test
    void duplicateMethodPathFailsDuringBuild() {
        ScopedPlatform platform = new ScopedPlatform("interface-servant-" + PLATFORM_IDS.incrementAndGet());
        ScopedApplication application = platform.newApplication();
        ScopedModule module = application.newModule();
        platform.registry().register(TransportProtocol.class, Http1Protocol.NAME, protocol());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> InterfaceServantGroup.<DuplicateService>builder()
                        .targetType(DuplicateService.class)
                        .service(new DuplicateServiceImpl())
                        .module(module)
                        .protocol(Http1Protocol.NAME)
                        .build()
        );

        assertTrue(exception.getMessage().contains("Duplicate RPC method path"));
    }

    private static TransportProtocol protocol() {
        return (TransportProtocol) Proxy.newProxyInstance(
                InterfaceServantGroupTest.class.getClassLoader(),
                new Class<?>[]{TransportProtocol.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "name" -> Http1Protocol.NAME;
                    case "toString" -> "test-protocol";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                }
        );
    }

    private interface DuplicateService {

        String find(String id);

        String find(int id);
    }

    private static final class DuplicateServiceImpl implements DuplicateService {

        @Override
        public String find(String id) {
            return id;
        }

        @Override
        public String find(int id) {
            return String.valueOf(id);
        }
    }
}
