package io.effi.rpc.test.filter;

import io.effi.rpc.contract.filter.FilterSupport;
import org.junit.jupiter.api.Test;

/**
 * @Author WenBo Zhou
 * @Date 2025/1/20 11:11
 */
public class FilterTest {

    @Test
    public void addFilter() {
        FilterSupport.Type type = FilterSupport.getType(new CallerReqFilter());
        System.out.println(type);
    }
}
