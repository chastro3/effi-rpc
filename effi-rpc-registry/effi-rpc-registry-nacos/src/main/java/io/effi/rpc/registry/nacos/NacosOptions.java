package io.effi.rpc.registry.nacos;

import io.effi.rpc.option.OptionName;
import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines Nacos registry-specific options.
 */
public interface NacosOptions {

    OptionName<String> PROJECT_NAME = STRING.onlyCurrent("registry.nacos.projectName");
}
