package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER_ARRAY;
import static io.effi.rpc.option.OptionTypes.STRING;
import static io.effi.rpc.option.OptionTypes.STRING_ARRAY;

/**
 * Defines servant options.
 */
public interface ServantOptions {

    OptionName<String> LABEL = STRING.currentFirst("servant.label");

    OptionName<Integer[]> EXCLUDED_PORT = INTEGER_ARRAY.mergeParent("servant.excludedPort");

    OptionName<String[]> DECLARED_PROTOCOL = STRING_ARRAY.currentFirst("servant.protocol");
}
