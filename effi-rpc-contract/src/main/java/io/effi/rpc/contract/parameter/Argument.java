package io.effi.rpc.contract.parameter;

import io.effi.rpc.util.Holder;

import java.util.HashMap;
import java.util.Map;

/**
 * Make parameters.
 */
public interface Argument {

    /**
     * Represents the source from which the method field obtains its value when configuring {@link MethodMapper}.
     */
    class Source extends Holder<String> {

        public Source(String value) {
            super(value);
        }
    }

    /**
     * Represents the target location in the request where the parameters wrapped by this class are added.
     * Used during the caller invocation.
     */
    class Target extends Holder<Map<String, String>> {

        public Target() {
            super(new HashMap<>());
        }

        /**
         * Adds a key-value pair to the target map.
         *
         * @param key   the key to add
         * @param value the value associated with the key
         * @return the current Target instance for method chaining
         */
        public Target add(String key, String value) {
            get().put(key, value);
            return this;
        }
    }

}

