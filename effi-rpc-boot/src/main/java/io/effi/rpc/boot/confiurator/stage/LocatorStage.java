package io.effi.rpc.boot.confiurator.stage;


import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Locator;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;

import java.net.InetSocketAddress;

import static io.effi.rpc.boot.confiurator.stage.LocatorStage.NAME;

@Extension(NAME)
public class LocatorStage implements Stage.CallUnit<Request, Caller<?>> {

    public static final String NAME = "locatorStage";

    @Override
    public Interaction.Result process(CallContext<Request, Caller<?>> context, Chain chain) {
        Locator locator = context.peer().locator();
        InetSocketAddress remoteAddress = locator.locate(context);
        context.message().url().withAddress(remoteAddress);
        return chain.proceed(context);
    }
}
