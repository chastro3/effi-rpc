package io.effi.rpc.serialization.jdk;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.serialization.AbstractSerializer;

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
import static io.effi.rpc.config.OptionName.Strategy.CURRENT_FIRST;
import static io.effi.rpc.config.OptionName.Strategy.MERGE_PARENT;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using Jdk.
 */
@Extension(NAME)
public class JdkSerializer extends AbstractSerializer implements ScopedPlatform.Acceptor {

    public static final String NAME = "jdk";

    public static final List<String> DEFAULT_ALLOWED_PACKAGES = List.of(
            "io.effi.rpc.",
            "java.lang.",
            "java.util.",
            "java.time."
    );

    public static final OptionName<List<String>> ALLOWED_PACKAGES =
            OptionName.of("serializer.jdk.allowedPackages", CURRENT_FIRST, DEFAULT_ALLOWED_PACKAGES);

    public static final OptionName<List<String>> INCLUDE_PACKAGES =
            OptionName.of("serializer.jdk.includePackages", MERGE_PARENT, List.of());

    private volatile ObjectInputFilter inputFilter = createInputFilter(DEFAULT_ALLOWED_PACKAGES);

    @Override
    public void accept(ScopedPlatform platform) {
        List<String> allowedPackages = new ArrayList<>(platform.options().option(ALLOWED_PACKAGES));
        allowedPackages.addAll(platform.options().option(INCLUDE_PACKAGES));
        inputFilter = createInputFilter(allowedPackages);
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
}
