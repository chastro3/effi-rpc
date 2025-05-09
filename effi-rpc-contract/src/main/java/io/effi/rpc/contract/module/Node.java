package io.effi.rpc.contract.module;

import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.Lifecycle;
import io.effi.rpc.util.LifecycleConfiguration;
import io.effi.rpc.spi.ExtensionLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a lifecycle-managed node with a parent-child structure.
 */
public abstract class Node extends AbstractAttributes implements Lifecycle {

    protected final Object lock = new Object();

    protected String name;

    protected Node parent;

    protected volatile Map<String, Node> children;

    protected List<LifecycleConfiguration<Node>> configurations;

    protected volatile State state;

    @SuppressWarnings("unchecked")
    protected void initialize(String name, Node parent, Class<?> configurationType) {
        this.name = name;
        this.parent = parent;
        this.configurations = (List<LifecycleConfiguration<Node>>) ExtensionLoader.loadExtensions(configurationType);
        init();
    }

    @Override
    public void init() {
        if (state == null) {
            synchronized (this) {
                if (state == null) {
                    for (LifecycleConfiguration<Node> configuration : configurations) {
                        configuration.preInit(this);
                    }
                    if (doInit()) {
                        state = State.INITIALIZED;
                        for (LifecycleConfiguration<Node> configuration : configurations) {
                            configuration.postInit(this);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void start() {
        if (state == State.INITIALIZED || state == State.STOPPED) {
            synchronized (this) {
                if (state == State.INITIALIZED || state == State.STOPPED) {
                    for (LifecycleConfiguration<Node> configuration : configurations) {
                        configuration.preStart(this);
                    }
                    if (doStart()) {
                        state = State.STARTED;
                        for (LifecycleConfiguration<Node> configuration : configurations) {
                            configuration.postStart(this);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        if (state == State.STARTED) {
            synchronized (this) {
                if (state == State.STARTED) {
                    for (LifecycleConfiguration<Node> configuration : configurations) {
                        configuration.preStop(this);
                    }
                    if (doStop()) {
                        state = State.STOPPED;
                    }
                    for (LifecycleConfiguration<Node> configuration : configurations) {
                        configuration.postStop(this);
                    }
                }
            }
        }
    }

    public Node addChild(Node child) {
        if (child != null) {
            if (children == null) {
                synchronized (lock) {
                    if (children == null)
                        children = new HashMap<>();
                }
            }
            children.put(child.name(), child);
        }
        return this;
    }

    public Node name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public Node parent() {
        return parent;
    }

    public Map<String, Node> children() {
        return children;
    }

    protected boolean doInit() {
        return true;
    }

    protected boolean doStart() {
        return true;
    }

    protected boolean doStop() {
        return true;
    }

}
