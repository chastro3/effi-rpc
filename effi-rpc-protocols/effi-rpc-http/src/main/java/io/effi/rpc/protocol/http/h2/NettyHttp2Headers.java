package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.util.AssertUtil;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.ReadOnlyHttp2Headers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link HttpHeaders} using {@link Http2Headers}.
 */
public class NettyHttp2Headers implements HttpHeaders {

    private final Http2Headers headers;

    public NettyHttp2Headers() {
        this.headers = new DefaultHttp2Headers();
    }

    public NettyHttp2Headers(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
        AssertUtil.notNull(headers, "headers");
        if (headers instanceof Http2Headers http2Headers) {
            if (http2Headers instanceof ReadOnlyHttp2Headers) {
                this.headers = new DefaultHttp2Headers();
                this.headers.add(http2Headers);
            } else {
                this.headers = http2Headers;
            }
        } else if (headers instanceof NettyHttp2Headers httpHeaders) {
            this.headers = httpHeaders.headers();
        } else {
            this.headers = new DefaultHttp2Headers();
            add(headers);
        }
    }

    @Override
    public CharSequence get(CharSequence name) {
        return headers.get(name);
    }

    @Override
    public List<CharSequence> getAll(CharSequence name) {
        return headers.getAll(name);
    }

    @Override
    public void add(CharSequence name, CharSequence value) {
        headers.add(name, value);
    }

    @Override
    public void add(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
        if (headers != null) {
            if (headers instanceof Http2Headers http2Headers) {
                this.headers.add(http2Headers);
            } else if (headers instanceof NettyHttp2Headers http2Headers) {
                this.headers.add(http2Headers.headers());
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
            if (headers instanceof Http2Headers http2Headers) {
                this.headers.set(http2Headers);
            } else if (headers instanceof NettyHttp2Headers http2Headers) {
                this.headers.set(http2Headers.headers());
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
        return headers.iterator();
    }

    public Http2Headers headers() {
        return headers;
    }
}
