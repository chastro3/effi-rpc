package io.effi.rpc.boot.stage;


import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallStage;
import io.effi.rpc.base.context.StageChain;

import java.net.InetSocketAddress;

import static io.effi.rpc.boot.stage.LocatorStage.NAME;

@Extension(NAME)
public class LocatorStage implements CallStage<Message.Request, Caller<?>> {

    public static final String NAME = "locatorStage";

    @Override
    public Result execute(CallContext<Message.Request, Caller<?>> context, StageChain chain) {
        Locator locator = context.callSide().locator();
        InetSocketAddress remoteAddress = locator.locate(context);
        context.message().url().address(remoteAddress);
        return chain.proceed(context);
    }
}
