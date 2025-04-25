package io.effi.rpc.config;

/**
 * Represents various types of URLs used in the
 * application. Each type corresponds to a specific role or function
 * of a URL within the context of remote procedure calls or service
 * interactions.
 */
public enum URLType {

    /**
     * Represents the default URL type.
     */
    DEFAULT,

    /**
     * Represents a URL for a caller in a remote procedure call context.
     */
    CALLER,

    /**
     * Represents a URL for a callee in a remote procedure call context.
     */
    CALLEE,

    /**
     * Represents a URL for a request being made.
     */
    REQUEST,

    /**
     * Represents a URL for a client in a remote procedure call context.
     */
    CLIENT,

    /**
     * Represents a URL for a server in a remote procedure call context.
     */
    SERVER;

    /**
     * Checks if the given URL is of the current URL type.
     */
    public boolean match(URL url) {
        return url.type() == this;
    }
}

