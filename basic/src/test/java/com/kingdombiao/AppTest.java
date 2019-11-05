package com.kingdombiao;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws InterruptedException {
        CountDownLatch countDownLatch=new CountDownLatch(1);
        countDownLatch.countDown();

        countDownLatch.await();
    }
}
