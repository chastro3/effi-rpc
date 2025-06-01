package io.effi.rpc.protocol.http;

import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.base.filter.Filter;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.component.PlatformConfiguration;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.protocol.http.filter.HttpRequestFilter;

@Extension("http")
public class HttpInitializedConfiguration implements PlatformConfiguration {

    @Override
    public void preInit(EffiRpcPlatform platform) {
        platform.register(Filter.class, new HttpRequestFilter(), Tags.FORCE_ACTIVE);
    }
}
