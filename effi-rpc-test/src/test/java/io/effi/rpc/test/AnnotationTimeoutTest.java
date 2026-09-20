package io.effi.rpc.test;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.boot.AnnotationSupport;
import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.Caller;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnnotationTimeoutTest {

    @Test
    void defaultTimeoutUsesCallerDefault() throws NoSuchMethodException {
        Method method = AnnotatedClient.class.getDeclaredMethod("defaultTimeout");
        HierarchicalOptions options = HierarchicalOptions.create();

        AnnotationSupport.fillOption(method.getAnnotation(Call.class), options);

        assertEquals(Caller.TIMEOUT.defaultValue(), options.option(Caller.TIMEOUT));
    }

    @Test
    void explicitTimeoutOverridesCallerDefault() throws NoSuchMethodException {
        Method method = AnnotatedClient.class.getDeclaredMethod("explicitTimeout");
        HierarchicalOptions options = HierarchicalOptions.create();

        AnnotationSupport.fillOption(method.getAnnotation(Call.class), options);

        assertEquals(1500, options.option(Caller.TIMEOUT));
    }

    private static final class AnnotatedClient {

        @Call
        void defaultTimeout() {
        }

        @Call(timeout = 1500)
        void explicitTimeout() {
        }
    }
}
