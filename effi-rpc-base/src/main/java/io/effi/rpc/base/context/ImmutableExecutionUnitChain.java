package io.effi.rpc.base.context;

import io.effi.rpc.base.Result;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides an immutable linked chain of {@link ExecutionUnit} instances.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ImmutableExecutionUnitChain<U extends ExecutionUnit, CHAIN extends ImmutableExecutionUnitChain<U, CHAIN>>
        implements ExecutionUnitChain {

    private final String name;

    private final CHAIN next;

    private final U executionUnit;

    protected ImmutableExecutionUnitChain(String name, U executionUnit, CHAIN next) {
        this.name = name;
        this.executionUnit = executionUnit;
        this.next = next;
    }

    protected static <U extends ExecutionUnit, CHAIN extends ImmutableExecutionUnitChain<U, CHAIN>>
    CHAIN init(EffiRpcModule module, ExecutionUnitProvider<U> euProvider, String[] names, Factory<U, CHAIN> factory, CHAIN terminal) {
        AssertUtil.notNull(module, "module");
        AssertUtil.notNull(euProvider, "execution unit provider");
        AssertUtil.condition(CollectionUtil.isNotEmpty(names), "name(s) cannot be empty");
        Map<String, U> eus = new LinkedHashMap<>(names.length);
        for (String name : names) {
            if (StringUtil.isNotBlank(name)) {
                U eu = euProvider.provide(module, name);
                if (eu != null) {
                    eus.put(name, eu);
                }
            }
        }
        return init(eus, factory, terminal);
    }

    protected static <U extends ExecutionUnit, CHAIN extends ImmutableExecutionUnitChain<U, CHAIN>>
    CHAIN init(Map<String, U> eus, Factory<U, CHAIN> factory, CHAIN terminal) {
        AssertUtil.notNull(terminal, "terminal");
        if (CollectionUtil.isEmpty(eus)) return terminal;
        Map.Entry<String, U>[] entries = eus.entrySet().toArray(CollectionUtil.emptyEntryArray());
        CHAIN next = terminal;
        for (int i = entries.length - 1; i >= 0; i--) {
            Map.Entry<String, U> entry = entries[i];
            next = factory.create(entry.getKey(), entry.getValue(), next);
        }
        return next;
    }

    @Override
    public <C extends ExchangeContext> Result proceed(C context) {
        if (executionUnit == null) return null;
        return executionUnit.execute(context, next);
    }

    public CHAIN getChain(String name) {
        if (Objects.equals(name, this.name)) {
            return (CHAIN) this;
        }
        if (next == null) return null;
        return next.getChain(name);
    }

    public String name() {
        return name;
    }

    public CHAIN next() {
        return next;
    }

    /**
     * Creates a new chain node for the given execution unit.
     *
     * @param <U>     the execution unit type
     * @param <CHAIN> the chain type
     */
    @FunctionalInterface
    public interface Factory<U extends ExecutionUnit, CHAIN extends ImmutableExecutionUnitChain<U, CHAIN>> {

        /**
         * Creates a chain element with the given name, execution unit, and next chain node.
         *
         * @param name     the name of the execution unit
         * @param executor the execution unit instance
         * @param chain    the next chain node
         * @return the created chain node
         */
        CHAIN create(String name, U executor, CHAIN chain);
    }

    /**
     * Provides execution units by name from a module.
     *
     * @param <U> the execution unit type
     */
    @FunctionalInterface
    public interface ExecutionUnitProvider<U extends ExecutionUnit> {

        /**
         * Provides an execution unit for the given module and name.
         *
         * @param module the module
         * @param name   the name of the execution unit
         * @return the execution unit instance or null if not found
         */
        U provide(EffiRpcModule module, String name);
    }


}

