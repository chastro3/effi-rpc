package io.effi.rpc.base;

import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLSource;

/**
 * Represents a message envelope containing a request URL and body.
 */
public interface Envelope extends URLSource {

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
    interface Request extends Envelope {

        /**
         * Checks if a reply is required.
         */
        boolean needReply();
    }

    /**
     * Represents a response message.
     */
    interface Response extends Envelope {

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


