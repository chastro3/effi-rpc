package io.effi.rpc.boot.confiurator.stage;


import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;
import io.effi.rpc.exception.EffiRpcException;

import static io.effi.rpc.boot.confiurator.stage.InvokeCalleeStage.NAME;

@Extension(NAME)
public class InvokeCalleeStage implements Stage.CallUnit<Request, Callee> {

    public static final String NAME = "invokeCalleeStage";

    @Override
    public Interaction.Result process(CallContext<Request, Callee> context, Chain chain) {
        SmartURL url = context.message().url();
        Callee callee = context.peer();
        Interaction.Result result = null;
        try {
            if (false) {
                //                long timeout = smartUrl.config().get(KeyConstant.TIMEOUT);
                //                String timestamp = smartUrl.config().get(KeyConstant.TIMESTAMP);
                //                LocalDateTime localDateTime = DateUtil.parse(timestamp);
                //                long margin = Duration.between(localDateTime, LocalDateTime.now()).toMillis();
                //                if (margin < timeout) {
                //                    returnValue = callee.invoke(context.args());
                //                    long invokeAfterMargin = Duration.between(localDateTime, LocalDateTime.now()).toMillis();
                //                    if (invokeAfterMargin > timeout) {
                //                        returnValue = null;
                //                    }
                //                }
            } else {
                result = Interaction.Result.success(url, callee.invoke(context.args()));
            }
        } catch (EffiRpcException e) {
            result = Interaction.Result.failure(url, e);
        }
        return result;
    }
}
