package io.effi.rpc.context.support;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides an immutable linked chain of execution units.
 * <p>
 * Implements an immutable chain structure for processing execution units
 * in a linked sequence with support for named chain elements and terminal nodes.
 *
 * @param <U>    the execution unit type
 * @param <CHAIN> the chain type
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ImmutableInteractionUnitChain<U extends Interaction.Unit, CHAIN extends ImmutableInteractionUnitChain<U, CHAIN>>
        implements Interaction.UnitChain {

    protected final String name;

    protected final CHAIN next;

    protected final U unit;

    protected ImmutableInteractionUnitChain(String name, U unit, CHAIN next) {
        this.name = name;
        this.unit = unit;
        this.next = next;
    }

    /**
     * Initializes a chain from the given execution unit supplier and names.
     *
     * @param module     the scoped module
     * @param euSupplier the supplier for execution units
     * @param names      the names of execution units to include
     * @param factory    the factory for creating chain nodes
     * @param tail       the tail chain node
     * @param <U>        the execution unit type
     * @param <CHAIN>    the chain type
     * @return the initialized chain
     */
    protected static <U extends Interaction.Unit, CHAIN extends ImmutableInteractionUnitChain<U, CHAIN>>
    CHAIN init(
            ScopedModule module,
            InteractionUnitSupplier<U> supplier,
            String[] names,
            Factory<U, CHAIN> factory,
            CHAIN tail) {
        AssertUtil.notNull(module, "module");
        AssertUtil.notNull(supplier, "interaction unit supplier");
        AssertUtil.valid(CollectionUtil.isNotEmpty(names), "id(s) cannot be empty");
        Map<String, U> units = new LinkedHashMap<>(names.length);
        for (String name : names) {
            if (StringUtil.isNotBlank(name)) {
                U eu = supplier.supply(module, name);
                if (eu != null) {
                    units.put(name, eu);
                }
            }
        }
        return init(units, factory, tail);
    }

    /**
     * Initializes a chain from the given execution units map.
     *
     * @param eus     the map of execution units
     * @param factory the factory for creating chain nodes
     * @param tail    the tail chain node
     * @param <U>     the execution unit type
     * @param <CHAIN> the chain type
     * @return the initialized chain
     */
    protected static <U extends Interaction.Unit, CHAIN extends ImmutableInteractionUnitChain<U, CHAIN>>
    CHAIN init(Map<String, U> units,
               Factory<U, CHAIN> factory,
               CHAIN tail) {
        AssertUtil.notNull(tail, "tail");
        if (CollectionUtil.isEmpty(units)) return tail;
        Map.Entry<String, U>[] entries = units.entrySet().toArray(CollectionUtil.emptyEntryArray());
        CHAIN next = tail;
        for (int i = entries.length - 1; i >= 0; i--) {
            Map.Entry<String, U> entry = entries[i];
            next = factory.create(entry.getKey(), entry.getValue(), next);
        }
        return next;
    }

    /**
     * Searches this chain and subsequent chains for the given id.
     * Returns null if not found. Recommended to start from the head.
     *
     * @param name the chain id to find
     * @return the matching chain or {@code null} if none found
     */
    public CHAIN lookupChain(String name) {
        if (Objects.equals(name, this.name)) {
            return (CHAIN) this;
        }
        if (next == null) return null;
        return next.lookupChain(name);
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
    public interface Factory<U extends Interaction.Unit, CHAIN extends ImmutableInteractionUnitChain<U, CHAIN>> {

        /**
         * Creates a chain element with the given id, execution unit, and next chain node.
         *
         * @param name     the id of the execution unit
         * @param executor the execution unit instance
         * @param chain    the next chain node
         * @return the created chain node
         */
        CHAIN create(String name, U executor, CHAIN chain);
    }

    /**
     * Supplies execution units by id from a module.
     */
    @FunctionalInterface
    public interface InteractionUnitSupplier<U extends Interaction.Unit> {

        /**
         * Supplies an execution unit for the given module and id.
         *
         * @param module the module
         * @param name   the id of the execution unit
         * @return the execution unit instance or null if not found
         */
        U supply(ScopedModule module, String name);
    }


}

