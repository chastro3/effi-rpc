package io.effi.rpc.option;

/**
 * Defines how an option value is resolved across scoped parents.
 */
public enum OptionStrategy {

    /**
     * Reads only the current scope.
     */
    ONLY_CURRENT,

    /**
     * Prefers the current scope and falls back to its parent.
     */
    CURRENT_FIRST,

    /**
     * Prefers the parent scope and falls back to the current scope.
     */
    PARENT_FIRST,

    /**
     * Merges the parent and current values.
     */
    MERGE_PARENT
}
