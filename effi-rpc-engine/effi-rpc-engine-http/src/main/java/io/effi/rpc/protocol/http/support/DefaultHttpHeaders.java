package io.effi.rpc.protocol.http.support;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Default implementation of {@link HttpHeaders}.
 */
public class DefaultHttpHeaders implements HttpHeaders {

    public final Map<CharSequence, CharSequence> headers = new HashMap<>();

    @Override
    public CharSequence get(CharSequence name) {
        return headers.get(name);
    }

    @Override
    public void add(CharSequence name, CharSequence value) {
        headers.put(name, value);
    }

    @Override
    public void addIfAbsent(CharSequence name, CharSequence value) {
        headers.putIfAbsent(name, value);
    }

    @Override
    public void add(Map<CharSequence, CharSequence> headers) {
        this.headers.putAll(headers);
    }

    @Override
    public @NotNull Iterator<Map.Entry<CharSequence, CharSequence>> iterator() {
        return headers.entrySet().iterator();
    }
}
