package io.effi.rpc.protocol.http.support;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines a mutable collection of HTTP headers.
 */
public interface HttpHeaders extends Iterable<Map.Entry<CharSequence, CharSequence>> {

    /**
     * Retrieves the first value of the specified header.
     *
     * @param name the header id
     * @return the first header value or null if not present
     */
    CharSequence get(CharSequence name);

    /**
     * Retrieves all values associated with the specified header id.
     *
     * @param name the header id
     * @return list of values, or empty list if not present
     */
    List<CharSequence> getAll(CharSequence name);

    /**
     * Adds a header or appends a value to an existing one.
     *
     * @param name  the header id
     * @param value the header value
     */
    void add(CharSequence name, CharSequence value);

    /**
     * Adds multiple headers from a map.
     *
     * @param headers the header map
     */
    void add(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers);

    /**
     * Sets a header, replacing any existing values for the given id.
     *
     * @param name  the header id
     * @param value the header value
     */
    void set(CharSequence name, CharSequence value);

    /**
     * Sets multiple values for the specified header, replacing any existing values.
     *
     * @param name   the header id
     * @param values the header values
     */
    void set(CharSequence name, List<CharSequence> values);

    /**
     * Sets multiple headers, replacing any existing values for the given names.
     *
     * @param headers the header map
     */
    void set(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers);

    /**
     * Removes the header with the specified id.
     *
     * @param name the header id
     */
    void remove(CharSequence name);

    /**
     * Returns true if the header with the specified id is present.
     *
     * @param name the header id
     * @return true if present, false otherwise
     */
    boolean contains(CharSequence name);

    /**
     * Returns the first value of the specified header or the default value if not present.
     *
     * @param name         the header id
     * @param defaultValue the default value to return if the header is not present
     * @return the first header value or the default value if not present
     */
    default CharSequence getOrDefault(CharSequence name, CharSequence defaultValue) {
        CharSequence value = get(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Returns a set of all header names.
     */
    default Set<CharSequence> names() {
        Set<CharSequence> result = new HashSet<>();
        for (Map.Entry<CharSequence, CharSequence> entry : this) {
            result.add(entry.getKey());
        }
        return result;
    }

    /**
     * Returns true if no headers are present.
     */
    default boolean isEmpty() {
        return !iterator().hasNext();
    }
}


