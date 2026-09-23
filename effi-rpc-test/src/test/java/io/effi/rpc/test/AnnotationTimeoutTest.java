package io.effi.rpc.test;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.boot.AnnotationSupport;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.context.Caller;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import io.effi.rpc.context.options.CallerOptions;

class AnnotationTimeoutTest {

    @Test
    void defaultTimeoutUsesCallerDefault() throws NoSuchMethodException {
        Method method = AnnotatedClient.class.getDeclaredMethod("defaultTimeout");
        HierarchicalOptions options = HierarchicalOptions.create();

        AnnotationSupport.fillOption(method.getAnnotation(Call.class), options);

        assertEquals(CallerOptions.TIMEOUT.defaultValue(), options.option(CallerOptions.TIMEOUT));
    }

    @Test
    void explicitTimeoutOverridesCallerDefault() throws NoSuchMethodException {
        Method method = AnnotatedClient.class.getDeclaredMethod("explicitTimeout");
        HierarchicalOptions options = HierarchicalOptions.create();

        AnnotationSupport.fillOption(method.getAnnotation(Call.class), options);

        assertEquals(1500, options.option(CallerOptions.TIMEOUT));
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
