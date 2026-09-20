package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationServiceRegistrarTest {

    @Test
    void bindFailureKeepsRegistrarInactive() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            ScopedPlatform platform = new ScopedPlatform("bind-failure-platform");
            ScopedApplication application = platform.newApplication("bind-failure-application");
            ServerLauncher.attach(
                    application,
                    Http1ServerConfig.defaultConfig(),
                    occupied.getInetAddress().getHostAddress(),
                    occupied.getLocalPort()
            );
            ApplicationServiceRegistrar registrar = new ApplicationServiceRegistrar(application);

            Future<Void> result = registrar.register();

            assertThrows(CompletionException.class, () -> result.toCompletableFuture().join());
            assertFalse(registrar.active());
        }
    }
}
