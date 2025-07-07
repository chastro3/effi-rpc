package io.effi.rpc.boot.stage;


import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallStage;
import io.effi.rpc.base.context.StageChain;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.DateUtil;

import java.time.Duration;
import java.time.LocalDateTime;

import static io.effi.rpc.boot.stage.InvokeCalleeStage.NAME;

@Extension(NAME)
public class InvokeCalleeStage implements CallStage<Message.Request, Callee> {

    public static final String NAME = "invokeCalleeStage";

    @Override
    public Result execute(CallContext<Message.Request, Callee> context, StageChain chain) {
        URL url = context.message().url();
        Callee callee = context.callSide();
        Object returnValue = null;
        try {
            if (URLType.CALLER.match(url)) {
                long timeout = url.getLongParam(KeyConstant.TIMEOUT);
                String timestamp = url.getParam(KeyConstant.TIMESTAMP);
                LocalDateTime localDateTime = DateUtil.parse(timestamp);
                long margin = Duration.between(localDateTime, LocalDateTime.now()).toMillis();
                if (margin < timeout) {
                    returnValue = callee.invoke(context.args());
                    long invokeAfterMargin = Duration.between(localDateTime, LocalDateTime.now()).toMillis();
                    if (invokeAfterMargin > timeout) {
                        returnValue = null;
                    }
                }
            } else {
                returnValue = callee.invoke(context.args());
            }
        } catch (EffiRpcException e) {
            returnValue = e;
        }
        return Result.create(url, returnValue);
    }
}
