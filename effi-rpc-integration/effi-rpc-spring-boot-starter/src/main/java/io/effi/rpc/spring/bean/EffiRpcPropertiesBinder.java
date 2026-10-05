package io.effi.rpc.spring.bean;

import io.effi.rpc.spring.properties.EffiRpcProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * Binds Effi RPC configuration before configuration-property beans are initialized.
 */
final class EffiRpcPropertiesBinder {

    private EffiRpcPropertiesBinder() {
    }

    static EffiRpcProperties bind(Environment environment) {
        return Binder.get(environment)
                .bind("effi.rpc", Bindable.of(EffiRpcProperties.class))
                .orElseGet(EffiRpcProperties::defaults);
    }
}
