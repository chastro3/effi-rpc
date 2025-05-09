package io.effi.rpc.registry;

import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.config.URL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Registers metadata for a given URL using registered metadata handlers.
 * <p>
 * Processes the URL and executes the associated task.
 * </p>
 */
public class RegisterTask implements Runnable {

    private static final List<MetaDataRegister> REGISTER_META_DATA = ExtensionLoader.loadExtensions(MetaDataRegister.class);

    private final URL url;

    private final BiConsumer<RegisterTask, Map<String, String>> task;

    private boolean isFirstRun = true;

    public RegisterTask(URL url, BiConsumer<RegisterTask, Map<String, String>> task) {
        this.url = url;
        this.task = task;
        run();
    }

    public URL url() {
        return url;
    }

    public BiConsumer<RegisterTask, Map<String, String>> task() {
        return task;
    }

    public RegisterTask firstRun(boolean firstRun) {
        isFirstRun = firstRun;
        return this;
    }

    public boolean isFirstRun() {
        return isFirstRun;
    }

    @Override
    public void run() {
        Map<String, String> metaData = new HashMap<>();
        REGISTER_META_DATA.forEach(metaDataRegister -> metaDataRegister.process(url, metaData));
        task.accept(this, metaData);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
