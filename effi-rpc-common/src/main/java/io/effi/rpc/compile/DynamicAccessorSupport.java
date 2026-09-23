package io.effi.rpc.compile;

final class DynamicAccessorSupport {

    private DynamicAccessorSupport() {
    }

    static void validateInvocation(String[] methodNames, Class<?>[][] parameterTypes, int index, Object[] args) {
        if (methodNames == null || index < 0 || index >= methodNames.length) {
            throw new IllegalArgumentException("Method index out of range: " + index);
        }
        Class<?>[] types = parameterTypes[index];
        int expected = types == null ? 0 : types.length;
        if (expected == 0) {
            return;
        }
        if (args == null || args.length != expected) {
            throw new IllegalArgumentException(
                    "Invalid arguments for method: " + methodNames[index] + " (expected " + expected + ")"
            );
        }
        for (int i = 0; i < types.length; i++) {
            if (types[i].isPrimitive() && args[i] == null) {
                throw new IllegalArgumentException(
                        "Null argument at index " + i + " for method: " + methodNames[index]
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
        throw (T) throwable;
    }

    static void rethrowIfFatal(Throwable throwable) {
        if (throwable instanceof Error error && !(throwable instanceof LinkageError)) {
            throw error;
        }
    }
}
