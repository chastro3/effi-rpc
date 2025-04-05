package io.effi.rpc.engine;

import io.effi.rpc.common.config.Config;
import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.spi.ExtensionLoader;
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

    public String name() {
        return name;
    }

    public AnnotationStyleParser parser() {
        return parser;
    }
}
