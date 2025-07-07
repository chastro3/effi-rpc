package io.effi.rpc.component;

import io.effi.rpc.config.SystemConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.SystemKeys;
import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages platform-level resources and lifecycle.
 */
public final class EffiRpcPlatform extends ScopedContext {

    private static final Object LOCK = new Object();

    private static volatile EffiRpcPlatform INSTANCE;

    private static final String VERSION = loadVersion();

    private static final AtomicInteger NUM = new AtomicInteger(0);

    private EffiRpcPlatform(String name) {
        INSTANCE = this;
        name(name);
        initialize(PLATFORM, null, null, PlatformConfiguration.class);
    }

    public static EffiRpcPlatform getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    new EffiRpcPlatform(Component.DEFAULT);
                }
            }
        }
        return INSTANCE;
    }

    public String version() {
        return VERSION;
    }

    public EffiRpcApplication getApplication(String name) {
        return lookup(EffiRpcApplication.class, name);
    }

    public EffiRpcApplication newApplication() {
        return newApplication(null);
    }

    private static String loadVersion() {
        String path = Constant.INTERNAL_PATH + "version";
        try {
            InputStream stream = ClassUtil.getClassLoader(EffiRpcPlatform.class).getResourceAsStream(path);
            if (stream == null) {
                throw new IllegalStateException("Can't find " + path + " file");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return reader.readLine();
        } catch (Exception e) {
            throw new IllegalStateException(Messages.parseFile(path), e);
        }
    }

    public EffiRpcApplication newApplication(String name) {
        return newApplication(name, null);
    }

    public EffiRpcApplication newApplication(String name, ComponentStore repository) {
        if (StringUtil.isBlank(name)) {
            name = "application-" + NUM.incrementAndGet();
        }
        EffiRpcApplication application = new EffiRpcApplication(name, this, repository);
        register(EffiRpcApplication.class, name, application);
        return application;
    }

    public Collection<EffiRpcApplication> applications() {
        return listOf(EffiRpcApplication.class);
    }

    @Override
    protected void doStart() {
        applications().forEach(EffiRpcApplication::start);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    @Override
    protected void doStop() {
        applications().forEach(EffiRpcApplication::stop);
    }

    @Override
    protected void doInit() {
        String name = SystemConfig.getInstance().getParam(SystemKeys.PLATFORM_NAME, Component.DEFAULT);
        name(name);
    }

    /**
     * Provides access to the {@link EffiRpcPlatform}.
     */
    public interface Provider {

        /**
         * Returns the associated {@link EffiRpcPlatform}.
         */
        EffiRpcPlatform platform();
    }

    /**
     * Holds a reference to an {@link EffiRpcPlatform}.
     */
    public abstract static class Holder implements Provider {

        protected EffiRpcPlatform platform;

        public Holder(EffiRpcPlatform platform) {
            this.platform = platform == null ? EffiRpcPlatform.getInstance() : platform;
        }

        @Override
        public EffiRpcPlatform platform() {
            return platform;
        }
    }

}
