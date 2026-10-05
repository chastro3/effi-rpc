package io.effi.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.serialization.AbstractSerializer;
import io.effi.rpc.serialization.options.KryoOptions;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.CollectionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static io.effi.rpc.serialization.kryo.KryoSerializer.NAME;

/**
 * Implements {@link io.effi.rpc.serialization.Serializer} using Kryo.
 */
@Extension(value = NAME, onClass = "com.esotericsoftware.kryo.Kryo", primary = true)
public class KryoSerializer extends AbstractSerializer implements ScopedPlatform.Acceptor {

    public static final String NAME = "kryo";

    private static final int BUFFER_SIZE = 1024 * 4;

    private final List<Consumer<Kryo>> registrations = new CopyOnWriteArrayList<>();

    // Published after platform acceptance so thread-local Kryo instances see the registrations.
    private volatile List<Class<?>> registeredClasses = List.of();

    // Kryo is not thread-safe, so each thread owns its Kryo, Input, and Output instances.
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(this::createKryo);

    @Override
    public void accept(ScopedPlatform platform) {
        List<String> classNames = new ArrayList<>();
        String[] registeredClassNames = platform.options().option(KryoOptions.REGISTERED_CLASS_NAMES);
        if (registeredClassNames != null) {
            Collections.addAll(classNames, registeredClassNames);
        }
        classNames.addAll(
                CollectionUtil.toHashSet(platform.options().option(KryoOptions.INCLUDE_CLASS_NAMES))
        );
        this.registeredClasses = resolveClasses(classNames);
    }

    /**
     * Registers a class before Kryo instances are used.
     *
     * @param type the class to register
     */
    public void register(Class<?> type) {
        registrations.add(kryo -> kryo.register(type));
    }

    /**
     * Registers a class and its serializer before Kryo instances are used.
     *
     * @param type       the class to register
     * @param serializer the serializer used for the class
     * @param <T>        the class type
     */
    public <T> void register(Class<T> type, Serializer<T> serializer) {
        registrations.add(kryo -> kryo.register(type, serializer));
    }

    @Override
    protected void doSerialize(Object obj, OutputStream out) throws IOException {
        try (Output output = new Output(out, BUFFER_SIZE)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeClassAndObject(output, obj);
        }
    }

    @Override
    protected Object doDeserialize(InputStream in, Type type) throws IOException {
        try (Input input = new Input(in)) {
            Kryo kryo = kryoThreadLocal.get();
            return kryo.readClassAndObject(input);
        }
    }

    private Kryo createKryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(true);
        kryo.setReferences(false);
        registeredClasses.forEach(kryo::register);
        registrations.forEach(registration -> registration.accept(kryo));
        return kryo;
    }

    private List<Class<?>> resolveClasses(List<String> classNames) {
        ClassLoader classLoader = ClassUtil.findClassLoader(KryoSerializer.class);
        List<Class<?>> classes = new ArrayList<>(classNames.size());
        for (String className : classNames) {
            try {
                classes.add(Class.forName(className, false, classLoader));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Kryo registered class not found: " + className, e);
            }
        }
        return List.copyOf(classes);
    }
}
