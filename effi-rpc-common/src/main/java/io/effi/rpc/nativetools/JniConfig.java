package io.effi.rpc.nativetools;

/**
 * Represents a configuration for jni in native config.
 *
 * @see <a href="https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/assets/jni-config-schema-v1.1.0.json">jni-config-schema-v1.1.0.json</a>
 */
public class JniConfig extends ReflectConfig {

    @Override
    public String name() {
        return "jni-config.json";
    }
}
