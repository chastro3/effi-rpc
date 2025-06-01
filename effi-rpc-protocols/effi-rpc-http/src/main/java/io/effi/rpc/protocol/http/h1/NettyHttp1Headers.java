package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.util.AssertUtil;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.ReadOnlyHttpHeaders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link HttpHeaders} using {@link io.netty.handler.codec.http.HttpHeaders}.
 */
public class NettyHttp1Headers implements HttpHeaders {

    private final io.netty.handler.codec.http.HttpHeaders headers;

    public NettyHttp1Headers() {
        this.headers = new DefaultHttpHeaders();
    }

    public NettyHttp1Headers(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
        AssertUtil.notNull(headers, "headers");
        if (headers instanceof io.netty.handler.codec.http.HttpHeaders httpHeaders) {
            if (httpHeaders instanceof ReadOnlyHttpHeaders) {
                this.headers = new DefaultHttpHeaders();
                this.headers.add(httpHeaders);
            } else {
                this.headers = httpHeaders;
            }
        } else if (headers instanceof NettyHttp1Headers httpHeaders) {
            this.headers = httpHeaders.headers();
        } else {
            this.headers = new DefaultHttpHeaders();
            add(headers);
        }
    }

    @Override
    public CharSequence get(CharSequence name) {
        return headers.get(name);
    }

    @Override
    public List<CharSequence> getAll(CharSequence name) {
        return new ArrayList<>(headers.getAll(name));
    }

    @Override
    public void add(CharSequence name, CharSequence value) {
        headers.add(name, value);
    }

    @Override
    public void add(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
        if (headers != null) {
            if (headers instanceof io.netty.handler.codec.http.HttpHeaders httpHeaders) {
                this.headers.add(httpHeaders);
            } else if (headers instanceof NettyHttp1Headers httpHeaders) {
                this.headers.add(httpHeaders.headers());
            } else {
                for (Map.Entry<? extends CharSequence, ? extends CharSequence> entry : headers) {
                    add(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public void set(CharSequence name, CharSequence value) {
        headers.set(name, value);
    }

    @Override
    public void set(CharSequence name, List<CharSequence> values) {
        headers.set(name, values);
    }

    @Override
    public void set(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
        if (headers != null) {
            if (headers instanceof io.netty.handler.codec.http.HttpHeaders httpHeaders) {
                this.headers.set(httpHeaders);
            } else if (headers instanceof NettyHttp1Headers httpHeaders) {
                this.headers.set(httpHeaders.headers());
            } else {
                for (Map.Entry<? extends CharSequence, ? extends CharSequence> entry : headers) {
                    set(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public void remove(CharSequence name) {
        headers.remove(name);
    }

    @Override
    public boolean contains(CharSequence name) {
        return headers.contains(name);
    }

    @Override
    public Iterator<Map.Entry<CharSequence, CharSequence>> iterator() {
        return headers.iteratorCharSequence();
    }

    public io.netty.handler.codec.http.HttpHeaders headers() {
        return headers;
    }
}
