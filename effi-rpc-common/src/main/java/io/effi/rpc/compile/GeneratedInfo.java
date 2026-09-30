package io.effi.rpc.compile;

/**
 * Stores metadata and bytecode for a generated class.
 */
public record GeneratedInfo(
        /** Package name of the generated class. */
        String pkg,
        /** Simple class name of the generated class. */
        String name,
        /** Bytecode of the generated class. */
        byte[] data
) {

    /**
     * Returns the fully qualified generated class name.
     */
    public String qualifiedName() {
        return pkg == null || pkg.isEmpty() ? name : pkg + "." + name;
    }
}
