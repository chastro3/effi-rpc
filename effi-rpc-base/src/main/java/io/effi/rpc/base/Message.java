package io.effi.rpc.base;

import io.effi.rpc.config.URL;

/**
 * Represents a message envelope containing a request URL and body.
 */
public interface Message extends URL.Provider {

    /**
     * Checks if this is an instance (not serialized).
     */
    boolean isInstance();

    /**
     * Returns the request URL.
     */
    @Override
    URL url();

    /**
     * Returns the message body.
     */
    Object body();

    /**
     * Represents a request message.
     */
    interface Request extends Message {

        /**
         * Checks if a reply is required.
         */
        boolean needReply();
    }

    /**
     * Represents a response message.
     */
    interface Response extends Message {

        /**
         * Returns the response code.
         */
        String code();

        /**
         * Returns the response message.
         */
        String message();

        /**
         * Checks if the response is successful.
         */
        boolean isSuccess();
    }
}


