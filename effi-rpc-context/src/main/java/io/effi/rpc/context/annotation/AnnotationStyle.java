package io.effi.rpc.context.annotation;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.Options;
import io.effi.rpc.context.Peer;
import io.effi.rpc.util.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches and provides {@link AnnotationStyleResolver} instance.
 */
public class AnnotationStyle {

    public static final AnnotationStyle UNKNOWN = new AnnotationStyle(null, null);

    private static final Object LOCK = new Object();

    private static final Map<String, AnnotationStyle> CACHE = new ConcurrentHashMap<>(4);

    private final String name;

    private final AnnotationStyleResolver resolver;

    AnnotationStyle(String name, AnnotationStyleResolver resolver) {
        this.name = name;
        this.resolver = resolver;
    }

    public static AnnotationStyle getInstance(Options options) {
        if (options == null) return UNKNOWN;
        return getInstance(options.option(Peer.ANNOTATION_STYLE));
    }

    public static AnnotationStyle getInstance(String name) {
        if (StringUtil.isBlank(name)) {
            return AnnotationStyle.UNKNOWN;
        }
        AnnotationStyle style = CACHE.get(name);
        if (style == null) {
            synchronized (LOCK) {
                style = CACHE.get(name);
                if (style == null) {
                    try {
                        AnnotationStyleResolver parser = ScopedPlatform.defaultInstance()
                                .namedExtension(AnnotationStyleResolver.class, name);
                        style = new AnnotationStyle(name, parser);
                        CACHE.put(name, style);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return style == null ? UNKNOWN : style;
    }

    public String name() {
        return name;
    }

    public AnnotationStyleResolver resolver() {
        return resolver;
    }
}
