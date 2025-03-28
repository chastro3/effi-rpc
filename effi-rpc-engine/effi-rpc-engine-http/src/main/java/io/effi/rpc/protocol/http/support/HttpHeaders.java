package io.effi.rpc.protocol.http.support;

import java.util.Map;

/**
 * HTTP headers, allowing for iteration over header entries.
 */
public interface HttpHeaders extends Iterable<Map.Entry<CharSequence, CharSequence>> {

    /**
     * Retrieves the value of the specified header by its name.
     * If the header does not exist, this method may return null or an empty value, depending on the implementation.
     *
     * @param name the name of the header to retrieve
     * @return the value of the specified header, or null if the header is not present
     */
    CharSequence get(CharSequence name);

    /**
     * Adds a new header or updates an existing header with the specified name and value.
     * This method allows for multiple values to be associated with the same header name.
     *
     * @param name  the name of the header to add
     * @param value the value of the header to add
     */
    void add(CharSequence name, CharSequence value);

    /**
     * Adds a new header only if the specified header name does not already exist.
     * This is useful for ensuring that existing headers are not overwritten.
     *
     * @param name  the name of the header to add
     * @param value the value of the header to add
     */
    void addIfAbsent(CharSequence name, CharSequence value);

    /**
     * Adds multiple headers from a provided map.
     * Each entry in the map is added as a header, allowing for bulk addition of headers.
     *
     * @param headers a map of headers to add, where the key is the header name and the value is the header value
     */
    void add(Map<CharSequence, CharSequence> headers);

    /**
     * Adds multiple headers from a provided map only if the headers are not already present.
     * This is a default method that iterates through the specified map and calls {@link #addIfAbsent(CharSequence, CharSequence)} for each entry.
     *
     * @param headers a map of headers to add if absent, where the key is the header name and the value is the header value
     */
    default void addIfAbsent(Map<CharSequence, CharSequence> headers) {
        for (CharSequence key : headers.keySet()) {
            addIfAbsent(key, headers.get(key));
        }
    }

    /**
     * Adds multiple headers from another {@link HttpHeaders} instance.
     * This is a default method that iterates through the specified headers and adds each header to the current instance.
     *
     * @param headers the {@link HttpHeaders} instance containing headers to add
     */
    default void add(HttpHeaders headers) {
        for (Map.Entry<CharSequence, CharSequence> header : headers) {
            add(header.getKey(), header.getValue());
        }
    }

}