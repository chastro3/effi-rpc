package io.effi.rpc.context.annotation;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.option.Options;
import io.effi.rpc.context.options.PeerOptions;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;

/**
 * Resolves an {@link AnnotationStyleResolver} for the owning platform.
 */
public class AnnotationStyle {

    public static final AnnotationStyle UNKNOWN = new AnnotationStyle(null, null);

    private final String name;

    private final AnnotationStyleResolver resolver;

    AnnotationStyle(String name, AnnotationStyleResolver resolver) {
        this.name = name;
        this.resolver = resolver;
    }

    /**
     * Resolves the annotation style configured by the supplied options.
     *
     * @param platform owning platform
     * @param options options carrying the style name
     * @return resolved annotation style
     */
    public static AnnotationStyle getInstance(ScopedPlatform platform, Options options) {
        if (options == null) return UNKNOWN;
        return getInstance(platform, options.option(PeerOptions.ANNOTATION_STYLE));
    }

    /**
     * Resolves the named annotation style from the owning platform.
     *
     * @param platform owning platform
     * @param name style name
     * @return resolved annotation style
     */
    public static AnnotationStyle getInstance(ScopedPlatform platform, String name) {
        if (StringUtil.isBlank(name)) {
            return UNKNOWN;
        }
        AssertUtil.notNull(platform, "platform");
        return new AnnotationStyle(
                name,
                platform.namedExtension(AnnotationStyleResolver.class, name)
        );
    }

    /**
     * Resolves the annotation style from the default platform.
     *
     * @param options options carrying the style name
     * @return resolved annotation style
     * @deprecated use {@link #getInstance(ScopedPlatform, Options)}
     */
    @Deprecated
    public static AnnotationStyle getInstance(Options options) {
        return getInstance(ScopedPlatform.defaultInstance(), options);
    }

    /**
     * Resolves the named annotation style from the default platform.
     *
     * @param name style name
     * @return resolved annotation style
     * @deprecated use {@link #getInstance(ScopedPlatform, String)}
     */
    @Deprecated
    public static AnnotationStyle getInstance(String name) {
        return getInstance(ScopedPlatform.defaultInstance(), name);
    }

    /**
     * Returns the annotation style name.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the annotation style resolver.
     */
    public AnnotationStyleResolver resolver() {
        return resolver;
    }
}
