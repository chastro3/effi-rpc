package io.effi.rpc.component.tools;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerTest {

    @Test
    void closingOneSchedulerDoesNotAffectAnother() throws Exception {
        Scheduler first = new Scheduler();
        Scheduler second = new Scheduler();
        try {
            CountDownLatch firstRan = new CountDownLatch(1);
            first.addDisposable(firstRan::countDown, 0, TimeUnit.MILLISECONDS);
            assertTrue(firstRan.await(1, TimeUnit.SECONDS));

            first.close();

            CountDownLatch secondRan = new CountDownLatch(1);
            second.addDisposable(secondRan::countDown, 0, TimeUnit.MILLISECONDS);
            assertTrue(secondRan.await(1, TimeUnit.SECONDS));
            assertTrue(second.active());
        } finally {
            first.close();
            second.close();
        }
    }
}
