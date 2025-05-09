package io.effi.rpc.contract.parameter;

import io.effi.rpc.util.Holder;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds arguments for RPC method mapping.
 */
public interface Argument {

    /**
     * Holds source expressions used to extract parameter values in {@link MethodMapper} mapping.
     */
    class Source extends Holder<String> {

        public Source(String value) {
            super(value);
        }
    }

    /**
     * Stores target parameters for injection during caller invocation.
     */
    class Target extends Holder<Map<String, String>> {

        public Target() {
            super(new HashMap<>());
        }

        /**
         * Adds key-value pairs to the target map.
         */
        public Target add(String key, String value) {
            get().put(key, value);
            return this;
        }
    }

}


