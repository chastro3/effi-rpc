package io.effi.rpc.registry;

import io.effi.rpc.common.extension.spi.ExtensionLoader;
import io.effi.rpc.common.url.URL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Register service task.
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

    /**
     * Returns the url.
     *
     * @return the url
     */
    public URL url() {
        return url;
    }

    /**
     * Returns the task.
     *
     * @return the task
     */
    public BiConsumer<RegisterTask, Map<String, String>> task() {
        return task;
    }

    /**
     * Sets the firstRun.
     *
     * @param firstRun the firstRun
     */
    public RegisterTask firstRun(boolean firstRun) {
        isFirstRun = firstRun;
        return this;
    }

    /**
     * Returns the isFirstRun.
     *
     * @return the isFirstRun
     */
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
