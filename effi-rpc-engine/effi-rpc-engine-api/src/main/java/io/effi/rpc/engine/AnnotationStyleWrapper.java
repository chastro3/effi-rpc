package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.annotation.AnnotationStyleParser;

public class AnnotationStyleWrapper {

    private String name;

    private AnnotationStyleParser parser;

    public AnnotationStyleWrapper(Config config) {
        String style = config.get(DefaultConfigKeys.STYLE);
        if (StringUtil.isNotBlank(style)) {
            this.name = style;
            this.parser = ExtensionLoader.loadExtension(AnnotationStyleParser.class, style);
        }
    }

    /**
     * Returns the name.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the parser.
     */
    public AnnotationStyleParser parser() {
        return parser;
    }
}
