package net.jcip.lock.spin;

import net.jcip.lock.spin.clh.CLHLock1;
import org.junit.Assert;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;

public class LockTest {

    static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        testLock(new CLHLock1());
    }

    private static void testLock(final AbstractLock clhLock) {
        final CyclicBarrier cb = new CyclicBarrier(10, new Runnable() {
            @Override
            public void run() {
                System.out.println(count);
                Assert.assertEquals(1000000,count);
                clhLock.printLog();
            }
        });
        for (int i = 0; i<10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doTestLock(clhLock);
                    try {
                        cb.await();
                    } catch (InterruptedException|BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    public static void doTestLock(Lock lock) {
        System.out.println(Thread.currentThread().getName() + ": ready to lock");
        try {
            lock.lock();
            for (int i = 0; i<100000; i++) ++count;
        } finally {
            System.out.println(Thread.currentThread().getName() + ": unlocked");
            lock.unlock();
        }
    }
}