package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;
import static io.effi.rpc.option.OptionTypes.STRING;
import static io.effi.rpc.option.OptionTypes.STRING_ARRAY;

/**
 * Defines governance options used during service location.
 */
public interface GovernanceOptions {

    OptionName<String> LOCATOR = STRING.currentFirst("governance.locator");

    OptionName<String> LOAD_BALANCER = STRING.currentFirst("governance.loadBalancer");

    OptionName<String[]> REGISTRY = STRING_ARRAY.mergeParent("governance.registry");

    OptionName<String> ROUTER = STRING.currentFirst("governance.router");

    OptionName<String> SERVICE_DISCOVERY = STRING.currentFirst("governance.serviceDiscovery");

    OptionName<Integer> HASH_KEY_INDEX = INTEGER.currentFirst("governance.hashKeyIndex", -1);

    OptionName<Integer> SERVICE_DISCOVERY_TIMEOUT = INTEGER.currentFirst("governance.serviceDiscoveryTimeout", 1000);
}
