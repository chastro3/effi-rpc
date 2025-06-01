package io.effi.rpc.component;

import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.collection.LazyMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages platform-level resources and lifecycle.
 */
public final class EffiRpcPlatform extends AbstractScopedComponentRepository {

    private static final Object LOCK = new Object();

    private static volatile EffiRpcPlatform CURRENT_PLATFORM;

    private static final String VERSION = loadVersion();

    private static final AtomicInteger NUM = new AtomicInteger(0);

    private final Map<GenericKey<?>, Resource<?>> resources = new LazyMap<>(ConcurrentHashMap::new);

    private EffiRpcPlatform(String name) {
        CURRENT_PLATFORM = this;
        name(name);
        initialize(PLATFORM, null, PlatformConfiguration.class);
    }

    public static EffiRpcPlatform currentPlatform() {
        if (CURRENT_PLATFORM != null) return CURRENT_PLATFORM;
        return init(Component.DEFAULT);
    }

    public static EffiRpcPlatform init(String name) {
        AssertUtil.notNull(name, "name");
        if (CURRENT_PLATFORM == null) {
            synchronized (LOCK) {
                if (CURRENT_PLATFORM == null) {
                    new EffiRpcPlatform(name);
                }
            }
        }
        return CURRENT_PLATFORM;
    }

    public String version() {
        return VERSION;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(GenericKey<T> key, Supplier<T> creator, Consumer<T> closer) {
        Resource<T> resource = (Resource<T>) resources.computeIfAbsent(key,
                k -> new Resource<>(creator.get(), closer));
        return resource.value();
    }

    public EffiRpcApplication getApplication(String name) {
        return lookup(EffiRpcApplication.class, name);
    }

    public EffiRpcApplication newApplication() {
        return newApplication(null);
    }

    public EffiRpcApplication newApplication(String name) {
        if (StringUtil.isBlank(name)) {
            name = "application-" + NUM.incrementAndGet();
        }
        EffiRpcApplication application = new EffiRpcApplication(name, this);
        register(EffiRpcApplication.class, name, application);
        return application;
    }

    public Collection<EffiRpcApplication> applications() {
        MultiComponent<EffiRpcApplication> repository = getMultiComponent(EffiRpcApplication.class);
        if (repository == null) {
            return Collections.emptyList();
        }
        return repository.components().stream().map(SingleComponent::component).toList();
    }

    @Override
    protected void doStart() {
        applications().forEach(EffiRpcApplication::start);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    @Override
    protected void doStop() {
        applications().forEach(EffiRpcApplication::stop);
        ExtensionLoader.clearLoader();
        for (Resource<?> resource : resources.values()) {
            @SuppressWarnings("unchecked")
            Consumer<Object> closer = (Consumer<Object>) resource.closer();
            closer.accept(resource.value());
        }
    }

    private static String loadVersion() {
        try {
            String path = Constant.INTERNAL_PATH + "version";
            InputStream stream = ClassUtil.getClassLoader(EffiRpcPlatform.class).getResourceAsStream(path);
            if (stream == null) {
                throw new IllegalStateException("Can't find " + path + " file");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return reader.readLine();
        } catch (Exception e) {
            throw new IllegalStateException("Can't read version", e);
        }
    }

    private record Resource<T>(T value, Consumer<T> closer) {

        Resource(T value, Consumer<T> closer) {
            this.value = AssertUtil.notNull(value, "value");
            this.closer = AssertUtil.notNull(closer, "closer");
        }

    }

}
