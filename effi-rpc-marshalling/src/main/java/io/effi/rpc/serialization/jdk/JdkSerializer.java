package io.effi.rpc.serialization.jdk;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.serialization.AbstractSerializer;
import io.effi.rpc.serialization.options.JdkOptions;
import io.effi.rpc.util.CollectionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static io.effi.rpc.serialization.jdk.JdkSerializer.NAME;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} for JDK object serialization.
 * <p>
 * Deserialization is restricted to the package allowlist configured through {@link JdkOptions}.
 */
@Extension(NAME)
public class JdkSerializer extends AbstractSerializer implements ScopedPlatform.Acceptor {

    public static final String NAME = "jdk";

    private volatile ObjectInputFilter inputFilter = createInputFilter(List.of(JdkOptions.DEFAULT_ALLOWED_PACKAGES));

    @Override
    public void accept(ScopedPlatform platform) {
        List<String> allowedPackages = new ArrayList<>(
                List.of(platform.options().option(JdkOptions.ALLOWED_PACKAGES))
        );
        allowedPackages.addAll(
                CollectionUtil.toHashSet(platform.options().option(JdkOptions.INCLUDE_PACKAGES))
        );
        inputFilter = createInputFilter(allowedPackages);
    }

    // Unwrap arrays and reject any class outside the configured package allowlist.
    private static ObjectInputFilter createInputFilter(List<String> allowedPackages) {
        return info -> {
            Class<?> type = info.serialClass();
            if (type == null) {
                return ObjectInputFilter.Status.UNDECIDED;
            }
            while (type.isArray()) {
                type = type.getComponentType();
            }
            String typeName = type.getName();
            for (String allowedPackage : allowedPackages) {
                if (typeName.startsWith(allowedPackage.trim())) {
                    return ObjectInputFilter.Status.ALLOWED;
                }
            }
            return ObjectInputFilter.Status.REJECTED;
        };
    }

    @Override
    protected void doSerialize(Object obj, OutputStream out) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
            objectOutputStream.writeObject(obj);
        }
    }

    @Override
    protected Object doDeserialize(InputStream in, Type type) throws IOException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
            objectInputStream.setObjectInputFilter(inputFilter);
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
