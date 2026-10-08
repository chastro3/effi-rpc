package io.effi.rpc.test;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.core.AnnotationSupport;
import io.effi.rpc.option.HierarchicalOptions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.context.options.FaultToleranceOptions;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.context.options.InterceptorOptions;

class AnnotationTimeoutTest {

    @Test
    void defaultTimeoutUsesCallerDefault() throws NoSuchMethodException {
        Method method = AnnotatedClient.class.getDeclaredMethod("defaultTimeout");
        HierarchicalOptions options = HierarchicalOptions.create();

        AnnotationSupport.apply(method.getAnnotation(Call.class), options);

        assertEquals(CallerOptions.TIMEOUT.defaultValue(), options.option(CallerOptions.TIMEOUT));
    }

    @Test
    void explicitTimeoutOverridesCallerDefault() throws NoSuchMethodException {
        Method method = AnnotatedClient.class.getDeclaredMethod("explicitTimeout");
        HierarchicalOptions options = HierarchicalOptions.create();

        AnnotationSupport.apply(method.getAnnotation(Call.class), options);

        assertEquals(1500, options.option(CallerOptions.TIMEOUT));
    }

    @Test
    void blankAnnotationValuesDoNotOverrideConfiguredDefaults() throws NoSuchMethodException {
        Method method = AnnotatedClient.class.getDeclaredMethod("defaultTimeout");
        HierarchicalOptions options = HierarchicalOptions.create();
        options.addOption(CallerOptions.ENDPOINT, "configured-endpoint");
        options.addOption(CallerOptions.PROTOCOL, "configured-protocol");

        AnnotationSupport.apply(method.getAnnotation(Call.class), options);

        assertEquals("configured-endpoint", options.option(CallerOptions.ENDPOINT));
        assertEquals("configured-protocol", options.option(CallerOptions.PROTOCOL));
    }

    @Test
    void callerOptionsMapToCoreOptions() throws NoSuchMethodException {
        Method method = AnnotatedClient.class.getDeclaredMethod("configured");
        HierarchicalOptions options = HierarchicalOptions.create();

        AnnotationSupport.apply(method.getAnnotation(Call.class), options);

        assertEquals(2, options.option(FaultToleranceOptions.RETRIES));
        assertEquals("roundRobin", options.option(GovernanceOptions.LOAD_BALANCER));
        assertEquals("canary", options.option(GovernanceOptions.ROUTER));
        assertEquals("registry-discovery", options.option(GovernanceOptions.SERVICE_DISCOVERY));
        assertEquals(0, options.option(GovernanceOptions.HASH_KEY_INDEX));
        assertArrayEquals(new String[]{"auth"}, options.option(InterceptorOptions.EXCLUDE));
    }

    private static final class AnnotatedClient {

        @Call
        void defaultTimeout() {
        }

        @Call(timeoutMillis = 1500)
        void explicitTimeout() {
        }

        @Call(
                retries = 2,
                loadBalancer = "roundRobin",
                router = "canary",
                serviceDiscovery = "registry-discovery",
                hashKeyIndex = 0,
                excludeInterceptors = {"auth"}
        )
        void configured() {
        }
    }
}
