package io.effi.rpc.contract.annotation;

import io.effi.rpc.common.config.Config;
import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.util.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches and provides {@link AnnotationStyleParser} instances.
 */
public class AnnotationStyle {

    public static final AnnotationStyle UNKNOWN = new AnnotationStyle(null, null);

    private static final Object LOCK = new Object();

    private static final Map<String, AnnotationStyle> annotationStyles = new ConcurrentHashMap<>(8);

    private final String name;

    private final AnnotationStyleParser parser;

    AnnotationStyle(String name, AnnotationStyleParser parser) {
        this.name = name;
        this.parser = parser;
    }

    public static AnnotationStyle getInstance(Config config) {
        if (config == null) return UNKNOWN;
        return getInstance(config.get(DefaultConfigKeys.ANNOTATION_STYLE));
    }

    public static AnnotationStyle getInstance(String name) {
        if (StringUtil.isBlank(name)) {
            return AnnotationStyle.UNKNOWN;
        }
        AnnotationStyle style = annotationStyles.get(name);
        if (style == null) {
            synchronized (LOCK) {
                style = annotationStyles.get(name);
                if (style == null) {
                    try {
                        AnnotationStyleParser parser = ExtensionLoader.loadExtension(AnnotationStyleParser.class, name);
                        style = new AnnotationStyle(name, parser);
                        annotationStyles.put(name, style);
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

    public AnnotationStyleParser parser() {
        return parser;
    }
}
