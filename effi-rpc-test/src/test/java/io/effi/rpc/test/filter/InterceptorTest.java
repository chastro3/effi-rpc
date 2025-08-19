package io.effi.rpc.test.filter;

import io.effi.rpc.context.UnitType;
import org.junit.jupiter.api.Test;

/**
 * @Author WenBo Zhou
 * @Date 2025/1/20 11:11
 */
public class InterceptorTest {

    @Test
    public void addFilter() {
        UnitType<?, ?> type = UnitType.extract(new CallerReqInterceptor());
        UnitType<?, ?> extract = UnitType.extract(new TestCallStage());
        System.out.println(type);
    }
}
